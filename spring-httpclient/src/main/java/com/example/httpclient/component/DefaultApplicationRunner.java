package com.example.httpclient.component;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("## ApplicationRunner");

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