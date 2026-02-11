package com.example.empty.runner;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(2)
public class DefaultApplicationRunner implements ApplicationRunner {

    @Autowired
    private Environment environment;

    /*
     * $ gradle bootRun --args='--opt-arg1=value1 --opt-arg2=value2 --non-opt-arg1 0123 non-opt-arg2 -s'
     * $ java -jar executable.jar --opt-arg1=value1 --opt-arg2=value2 --non-opt-arg1 0123 non-opt-arg2 -s
     * $ java -cp non-executable.jar <main-class-name> --opt-arg1=value1 --opt-arg2=value2 --non-opt-arg1 0123 non-opt-arg2 -s
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        var optNames = args.getOptionNames();
        for (var name : optNames) {
            log.info("option: name={}, value={}", name, args.getOptionValues(name));
        }

        var nonOptArgs = args.getNonOptionArgs();
        for (var arg : nonOptArgs) {
            log.info("non option args: {}", arg);
        }

        var srcArgs = args.getSourceArgs();
        for (var arg : srcArgs) {
            log.info("source arg: {}", arg);
        }

        log.info("=== profiles ===================================");
        log.info("Active profiles : "+ Arrays.toString(environment.getActiveProfiles()));
        log.info("Datasource driver : " + environment.getProperty("spring.datasource.driver-class-name"));
        log.info("Datasource url : " + environment.getProperty("spring.datasource.url"));
        log.info("Datasource username : " + environment.getProperty("spring.datasource.username"));
        log.info("Datasource password : " + environment.getProperty("spring.datasource.password"));
        log.info("Server Port : " + environment.getProperty("server.port"));
        log.info("Default Property : " + environment.getProperty("default.string"));
        log.info("Common Property : " + environment.getProperty("common.string"));
    }
}
