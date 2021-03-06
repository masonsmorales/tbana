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

package com.yolodata.tbana.hadoop.mapred.shuttl.bucket.search;


import com.splunk.shuttl.archiver.model.Bucket;
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkDataQuery;
import com.yolodata.tbana.hadoop.mapred.shuttl.bucket.BucketFinder;
import com.yolodata.tbana.hadoop.mapred.shuttl.index.Index;
import com.yolodata.tbana.testutils.FileSystemTestUtils;
import com.yolodata.tbana.testutils.TestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BucketFinderTest {

    private FileSystem fs;
    private Index index;

    @Before
    public void setUp() throws Exception {
        fs = FileSystem.getLocal(new Configuration());

        fs.delete(new Path(TestUtils.TEST_FILE_PATH),true);

        index = createIndexWithBuckets();
    }

    private Index createIndexWithBuckets() throws IOException {
        Path root = FileSystemTestUtils.createEmptyDir(fs);
        String [] directories = {"db_28800000_28810000_index","db_28810001_28820000_index","db_28820001_28830000_index"};
        FileSystemTestUtils.createDirectories(fs, root, directories);

        return new Index(root.toString(),root.getName());
    }

    @Test
    public void testGetBucketsByTimeRange() throws Exception {


        DateTime earliestTime = new DateTime(28810001);
        DateTime latestTime = new DateTime(28820000);

        BucketFinder bucketFinder = new BucketFinder(fs, index);
        List<Bucket> buckets = bucketFinder.search(new SplunkDataQuery(earliestTime, latestTime));

        assertEquals(1, buckets.size());
    }

    @Test
    public void testGetBucketsWithMaxResults() throws Exception {
        BucketFinder bucketFinder = new BucketFinder(fs, index, 1);
        List<Bucket> buckets = bucketFinder.search(new SplunkDataQuery());

        assertEquals(1, buckets.size());
    }

    @Test
    public void testGetBucketsWithListOfIndexes() throws Exception {
        Index index2 = createIndexWithBuckets();
        BucketFinder bucketFinder = new BucketFinder(fs, Arrays.asList(index,index2));
        List<Bucket> buckets = bucketFinder.search(new SplunkDataQuery());

        assertEquals(6, buckets.size());
    }
}
