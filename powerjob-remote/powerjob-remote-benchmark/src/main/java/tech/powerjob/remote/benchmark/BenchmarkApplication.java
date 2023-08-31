package tech.powerjob.remote.benchmark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test engineering
 * Used for remote protocol pressure test
 *
 * @author tjq
 * @since 2023/1/7
 */
@SpringBootApplication
public class BenchmarkApplication {

    public static void main(String[] args) {
        SpringApplication.run(BenchmarkApplication.class, args);
    }
}
