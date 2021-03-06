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

package com.yolodata.tbana.spark.RDD

import spark.SparkContext
import org.apache.hadoop.mapred.FileInputFormat
import com.yolodata.tbana.testutils.{HadoopFileTestUtils, TestUtils, TestConfigurations}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.scalatest.FunSuite
import com.yolodata.tbana.spark.LocalSparkContext
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkConf
import org.apache.hadoop.util.StringUtils

class ShuttlRDDTest extends FunSuite with LocalSparkContext {

  test("basic functionality") {
    sc = new SparkContext("local", "test")
    val conf : SplunkConf = TestConfigurations.getConfigurationWithShuttlSearch
    val shuttlRoot: Path = new Path("src/integration-test/resources/shuttl")

    conf.set("mapred.input.dir", StringUtils.escapeString(shuttlRoot.toString))
    val rdd = ShuttlRDD(sc,conf)

    assert(rdd.count() === 2)

    val path: Path = new Path(TestUtils.TEST_FILE_PATH.concat("ShuttlRDD.out"))
    rdd.saveAsTextFile(path.toString)

    val fs : FileSystem = FileSystem.getLocal(conf)
    val actual = HadoopFileTestUtils.readMapReduceOutputFile(fs,path)

    val expected = "(0,[_time, source, host, sourcetype, _raw, _meta])\n" +
      "(46,[1336330530, source::/some/path/to/some/file/small_loremIpsum.txt, host::periksson-mbp15-2.local, sourcetype::small_loremIpsum-too_small, Lorem ipsum dolor sit amet, c, _indextime::1336330530 timestamp::none punct::____,_])\n";

    assert(expected === actual, "The read content from shuttl is not correct")
  }
}
