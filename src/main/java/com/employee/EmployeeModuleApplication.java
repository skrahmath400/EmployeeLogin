package com.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

import com.common.redisconfig.RedisConfig;
@EnableCaching
@SpringBootApplication
@Import({RedisConfig.class
	})
@EntityScan(basePackages = {"com.employee.entity", "com.common.entity"})
public class EmployeeModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeModuleApplication.class, args);
	}

}
