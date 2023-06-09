package org.swzn.bibackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.swzn.bibackend.mapper")
public class BiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiBackendApplication.class, args);
    }

}
