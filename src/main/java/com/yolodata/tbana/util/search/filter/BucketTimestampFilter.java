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

package com.yolodata.tbana.util.search.filter;

import com.splunk.shuttl.archiver.model.BucketName;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class BucketTimestampFilter extends BucketFilter {

    private final long earliest;
    private final long latest;

    public BucketTimestampFilter(FileSystem fileSystem, long earliest, long latest) {
        super(fileSystem);
        this.earliest = earliest;
        this.latest = latest;
    }

    @Override
    public boolean accept(String path) throws IOException {
        if(!super.accept(path))
            return false;

        BucketName bucket = new BucketName((new Path(path)).getName());

        return bucketWithinTimeRange(bucket);
    }

    private boolean bucketWithinTimeRange(BucketName bucket) {

        if(bucket.getLatest() < earliest)
            return false;
        if (bucket.getEarliest() > latest)
            return false;

        return true;
    }
}
