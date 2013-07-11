package com.yolodata.tbana.util.search.filter;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class DirectoryFilter implements SearchFilter {

    private FileSystem fileSystem;

    public DirectoryFilter(FileSystem fileSystem) {

        this.fileSystem = fileSystem;
    }

    @Override
    public boolean accept(String path) throws IOException {
        return accept(new Path(path));
    }

    public boolean accept(Path path) throws IOException {
        FileStatus status = fileSystem.getFileStatus(path);

        return status.isDir();
    }
}