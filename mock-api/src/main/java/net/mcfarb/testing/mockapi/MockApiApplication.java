package net.mcfarb.testing.mockapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import net.mcfarb.testing.mockapi.config.MockApiConfiguration;

@SpringBootApplication
@EnableConfigurationProperties(MockApiConfiguration.class)
public class MockApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockApiApplication.class, args);
	}

}
