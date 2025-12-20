package net.mcfarb.testing.mockapi.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient used in fallback proxy requests.
 */
@Configuration
@EnableConfigurationProperties(MockApiConfiguration.class)
@RequiredArgsConstructor
public class WebClientConfiguration {

	private final MockApiConfiguration mockApiConfiguration;

	/**
	 * Creates a WebClient bean configured for making fallback requests to real endpoints.
	 * The client is configured with:
	 * - Connection timeout from configuration
	 * - Read/write timeouts
	 * - HTTP compression support
	 *
	 * @return Configured WebClient instance
	 */
	@Bean
	public WebClient webClient() {
		int timeoutMs = mockApiConfiguration.getFallback().getTimeoutMs();

		HttpClient httpClient = HttpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
				.responseTimeout(Duration.ofMillis(timeoutMs))
				.doOnConnected(conn ->
						conn.addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
								.addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
				)
				.compress(true);

		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}
}
