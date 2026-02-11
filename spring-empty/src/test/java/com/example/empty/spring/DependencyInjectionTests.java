package com.example.empty.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.empty.di.Formatter;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class DependencyInjectionTests {

    @Autowired
    private Formatter primaryFormatter;

    @Autowired
    @Qualifier("fooFormatter")
    private Formatter qualifierFormatter;


    @Test
    public void fieldDiTest() {
        System.out.println(primaryFormatter.toString()); //BarFormatter
        System.out.println(qualifierFormatter.toString()); //FooFormatter
    }

    @Test
    public void paramDiTest(@Autowired final Formatter zooFormatter) {
        System.out.println(zooFormatter.toString());
    }
}
