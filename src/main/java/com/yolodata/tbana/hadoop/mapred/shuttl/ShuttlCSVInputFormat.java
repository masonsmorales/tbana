/*
 * Copyright (c) 2013 Yolodata, LLC,  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yolodata.tbana.hadoop.mapred.shuttl;

import com.yolodata.tbana.hadoop.mapred.splunk.SplunkDataQuery;
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkDataQueryFactory;
import com.yolodata.tbana.hadoop.mapred.util.ArrayListTextWritable;
import com.yolodata.tbana.util.search.ShuttlCsvFileFinder;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShuttlCSVInputFormat extends FileInputFormat<LongWritable, ArrayListTextWritable> {

    @Override
    public RecordReader<LongWritable, ArrayListTextWritable> getRecordReader(
            InputSplit inputSplit, JobConf jobConf, Reporter reporter) throws IOException {
        reporter.setStatus(inputSplit.toString());

        ShuttlCSVRecordReader reader = new ShuttlCSVRecordReader();
        reader.initialize(inputSplit, jobConf);

        return reader;
    }

    public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
        List<InputSplit> splits = new ArrayList<InputSplit>();
        FileSystem fs = FileSystem.get(job);
        SplunkDataQuery dataQuery = SplunkDataQueryFactory.createWithJobConf(job);

        ShuttlCsvFileFinder fileFinder = new ShuttlCsvFileFinder(fs,getInputPaths(job)[0]);
        List<String> csvPaths = fileFinder.findFiles(dataQuery);

        long currentOffset = 0;

        for(String p : csvPaths)
        {
            FileStatus csvFile = fs.getFileStatus(new Path(p));
            List<CsvSplit> fileSplits = getSplitsForFile(csvFile, job, numSplits,currentOffset);
            currentOffset += csvFile.getLen();
            splits.addAll(fileSplits);
        }

        if(splits.size() >0)
            ((CsvSplit)splits.get(0)).setSkipHeader(false);

        return splits.toArray(new InputSplit[splits.size()]);
    }

    protected static List<CsvSplit> getSplitsForFile(FileStatus status, JobConf conf, int numSplits, long startKey)
            throws IOException {
        Path filePath = getPath(status);
        FileSystem fs = filePath.getFileSystem(conf);
        if(numSplits==0)
            numSplits++;
        return getFileSplitsFast(numSplits, filePath, fs, startKey);
    }

    private static List<CsvSplit> getFileSplitsFast(int numSplits, Path filePath, FileSystem fs, long startKey) {
        List<CsvSplit> splits = new ArrayList<CsvSplit>();

        try {
            long fileSize = fs.getFileStatus(filePath).getLen();
            long sizePerSplit = fileSize/(numSplits+1);

            long start=0,end=0;

            while(end<fileSize) {
                end = start+sizePerSplit;

                if(restOfFileChunkFitsInOneSplit(fileSize, end)) {
                    splits.add(new CsvSplit(filePath,start,end,startKey,true));
                    break;
                }

                // Seek to current length and find the new line
                FSDataInputStream in = fs.open(filePath);
                end = findEndOfLinePosition(in,end);

                splits.add(new CsvSplit(filePath,start,end,startKey,true));
                start=end;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return splits;
    }

    private static boolean restOfFileChunkFitsInOneSplit(long fileSize, long end) {
        return end >= fileSize;
    }

    // TODO: This is a heuristic that might not be true in all cases
    // We assume that a CSV line ends with "\n and does not start with a ,
    private static long findEndOfLinePosition(FSDataInputStream in, long end) throws IOException {
        in.seek(end);
        int c;
        String findNewLineBuffer = "";
        while((c=in.read())!= -1) {
            char ch = (char)c;

            // a real new line is found!
            if(findNewLineBuffer == "\"\n" && ch != ',')
                return in.getPos();

            if(findNewLineBuffer == "\"" && ch == '\n') {
                findNewLineBuffer = findNewLineBuffer.concat("\n");
                continue;
            }

            if(ch == '\"' && findNewLineBuffer.length()==0) {
                findNewLineBuffer = "\"";
                continue;
            }
            findNewLineBuffer = "";

        }
        return in.getPos();
    }

    private static CsvSplit createSplit(Path filePath, long start, long end) {
        return new CsvSplit(filePath,start,end);
    }

    private static Path getPath(FileStatus status) throws IOException {
        Path fileName = status.getPath();
        if (status.isDir()) {
            throw new IOException("Not a file: " + fileName);
        }
        return fileName;
    }

}
