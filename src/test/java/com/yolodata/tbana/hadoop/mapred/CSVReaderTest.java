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

package com.yolodata.tbana.hadoop.mapred;

import com.yolodata.tbana.hadoop.mapred.util.CSVReader;
import com.yolodata.tbana.testutils.FileTestUtils;
import com.yolodata.tbana.testutils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReaderTest {

    private CSVReader csvReader;
    private ArrayList<Text> actual;
    private ArrayList<Text> expected;

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(TestUtils.TEST_FILE_PATH));
        FileUtils.forceMkdir(new File(TestUtils.TEST_FILE_PATH));
        actual = new ArrayList<Text>();
        expected = new ArrayList<Text>();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(TestUtils.TEST_FILE_PATH));
    }

    @Test
    public void readLine_emptyFile_returnsEmptyText() throws IOException {
        String csvContent = "";
        expected.add(new Text());
        testFileContent(csvContent, expected);
    }


    @Test
    public void readLine_oneColumn_returnsOneText() throws IOException {
        String csvContent = "hello";
        expected.add(new Text("hello"));
        testFileContent(csvContent, expected);
    }

    @Test
    public void readLine_twoColumns_returnsTwoTexts() throws IOException {
        String csvContent = "hello,world";

        expected.add(new Text("hello"));
        expected.add(new Text("world"));
        testFileContent(csvContent,expected);
    }

    private void testFileContent(String csvContent, List<Text> expected) throws IOException {
        String filepath = FileTestUtils.getRandomTestFilepath();

        assert(FileTestUtils.createFileWithContent(filepath, csvContent));

        csvReader = new CSVReader(new FileReader(filepath));

        csvReader.readLine(actual);

        assert(actual.size() == expected.size());

        for(int i=0; i<actual.size(); i++)
            assert(actual.get(i).equals(expected.get(i)));

    }
}
