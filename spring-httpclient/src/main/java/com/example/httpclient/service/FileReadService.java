package com.example.httpclient.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;
import com.example.httpclient.SpringApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileReadService {

    /**
     * read file from process working dir(command launching dir when execute java -jar or run in IDE)
     */
    public void readFromProcessWorkingDir() {
        Path p = Paths.get(System.getProperty("user.dir"));

        log.info("process working dir:\n{}", p.toString());

        // read file content
        /*
        Path p = Paths.get(System.getProperty("user.dir"), "data", "file.txt");
        String content = Files.readString(p);
        System.out.println(content);
        */
    }

    /**
     * read file from executed file exsiting dir(jar file or IDE classes dir)
     */
    public void readFromExecDir() {
        try {
            URI uri = SpringApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path execPath = Paths.get(uri); // if jar file then 'jar' path, if IDE execution then 'classes' dir
            Path execDir = Files.isRegularFile(execPath) ? execPath.getParent() : execPath;
            log.info("class file dir:\n{}", execDir.toString());

            // read file content
            /*
            Path target = execDir.resolve("config.yml");
            String cfg = Files.readString(target);
            */
        } catch (Exception e) {
            log.error("Error reading file from exec dir", e);
        }
    }

    public void readFromJarInnerResource() {
        try (InputStream is = SpringApplication.class.getResourceAsStream("/application.yml")) {
        if (is == null) throw new IOException("resource not found");
        String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        log.info("read from jar inner resource:\n{}", text);
        } catch (IOException e) {
            log.error("Error reading resource from jar", e);
        }
    }

    /**
     * 실행된 파일의 경로를 반환합니다.
     * - fat JAR로 실행하면 JAR 파일의 경로를 반환합니다.
     * - IDE에서 실행하면 classes 디렉토리 경로를 반환합니다.
     * - 실패 시 프로세스의 working directory를 반환합니다.
     */
    public Path getExecutableFilePath() {
        // 1) 시도: 클래스의 CodeSource 위치
        try {
            URI uri = SpringApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            // 일반적인 경우: file:/path/to/app.jar 또는 file:/.../classes/
            try {
                Path execPath = Paths.get(uri);
                if (Files.isRegularFile(execPath) && execPath.toString().endsWith(".jar")) {
                    return execPath;
                }
            } catch (Exception ignore) {
                // 일부 실행환경에서는 Paths.get(uri)에서 실패할 수 있음
            }

            // 2) fat JAR 내부 경로(jar:file:/.../app.jar!/BOOT-INF/classes/) 패턴 처리
            String uriStr = uri.toString();
            int idx = uriStr.indexOf(".jar!");
            if (idx != -1) {
                String jarPart = uriStr.substring(0, idx + 4); // ...*.jar
                try {
                    URI jarUri = new URI(jarPart);
                    Path jarPath = Paths.get(jarUri);
                    if (Files.exists(jarPath)) return jarPath;
                } catch (Exception ignore) {
                }
            }
            // 만약 classes 디렉토리(IDE 실행)라면 위에서 처리된 execPath가 이미 반환되었거나 아래 fallback으로 간다
        } catch (Exception e) {
            log.debug("codeSource lookup failed", e);
        }

        // 3) java -jar 로 실행했을 때 JVM 인자에 남는 실행명 확인(sun.java.command)
        try {
            String cmd = System.getProperty("sun.java.command");
            log.debug("sun.java.command: {}", cmd);
            if (cmd != null) {
                String first = cmd.split(" ")[0];
                if (first.endsWith(".jar")) {
                    Path jarPath = Paths.get(first);
                    if (!jarPath.isAbsolute()) {
                        jarPath = Paths.get(System.getProperty("user.dir")).resolve(jarPath).normalize();
                    }
                    if (Files.exists(jarPath)) return jarPath;
                }
            }
        } catch (Exception e) {
            log.debug("sun.java.command lookup failed", e);
        }

        // 최후 fallback: process working directory
        log.warn("Unable to determine executable jar path; falling back to user.dir");
        return Paths.get(System.getProperty("user.dir"));
    }
}
