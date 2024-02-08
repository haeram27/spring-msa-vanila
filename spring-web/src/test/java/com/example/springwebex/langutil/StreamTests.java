package com.example.springwebex.langutil;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class StreamTests {

    @Test
    public void streamTest() {
        var arr = new String[] {"a", "b", "c"};
        var streamOfArrayPart = Arrays.stream(arr, 1, 3); // 1~2 요소 [b, c]
        streamOfArrayPart.forEach(k -> {
            log.info(k);
        });
    }
}
