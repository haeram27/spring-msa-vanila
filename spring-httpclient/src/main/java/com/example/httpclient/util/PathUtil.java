package com.example.httpclient.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathUtil {

    /**
     * read file from process working dir(command launching dir when execute java -jar or run in IDE)
     */
    public static Path processWorkingDirectory() {
        Path p = Paths.get(System.getProperty("user.dir"));
        // log.info("process working dir:\n{}", p.toString());

        return p;
    }
}
