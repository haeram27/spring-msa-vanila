package com.example.springsecex.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(2)
public class DefaultApplicationRunner implements ApplicationRunner {
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
    }
}
