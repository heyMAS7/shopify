package com.example.demo;

import com.example.demo.domain.WooCommerceProductDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}


//	@Bean
//	CommandLineRunner commandLineRunner(WebClient webClient){
//		return args -> {
//			Mono<JsonNode> jsonNodeFlux = webClient.get()
//					.uri("https://ecom-tiheymas-agdwu.wpcomstaging.com/wp-json/wc/v3/products")
//					.header("Authorization", "Basic Y2tfMzUyNGEyOGE5NWU2M2E2NThlZGMxNTBiNDlmODVlNzExNmJjYWExMzpjc180Y2U4MWEyYzJmOTllOGE1OWMxOWU1YTdmMzM4MDQxY2I4YWY4Mzg1")
//					.retrieve()
//					.bodyToMono(JsonNode.class);
//
//			webClient.put()
//					.uri("https://ecom-tiheymas-agdwu.wpcomstaging.com/wp-json/wc/v3/products/19")
//					.header("Authorization", "Basic Y2tfMzUyNGEyOGE5NWU2M2E2NThlZGMxNTBiNDlmODVlNzExNmJjYWExMzpjc180Y2U4MWEyYzJmOTllOGE1OWMxOWU1YTdmMzM4MDQxY2I4YWY4Mzg1")
//					.bodyValue(Map.of("stock_quantity","40"))
//					.retrieve()
//					.bodyToMono(JsonNode.class)
//					.block();
//
//
//
//			List<WooCommerceProductDTO> productDTO = webClient.get()
//					.uri(uriBuilder -> uriBuilder
//							.scheme("https")
//							.host("ecom-tiheymas-agdwu.wpcomstaging.com")
//							.path("/wp-json/wc/v3/products")
//							.queryParam("sku", "123")
//							.build())
//					.header("Authorization", "Basic Y2tfMzUyNGEyOGE5NWU2M2E2NThlZGMxNTBiNDlmODVlNzExNmJjYWExMzpjc180Y2U4MWEyYzJmOTllOGE1OWMxOWU1YTdmMzM4MDQxY2I4YWY4Mzg1")
//					.retrieve()
//					.bodyToMono(new ParameterizedTypeReference<List<WooCommerceProductDTO>>() {})
//					.block();
//
//
//			webClient.put()
//					.uri(uriBuilder -> uriBuilder
//							.scheme("https")
//							.host("ecom-tiheymas-agdwu.wpcomstaging.com")
//							.path("/wp-json/wc/v3/products/{id}")
//							.build("123"))
//					.header("Authorization", "Basic Y2tfMzUyNGEyOGE5NWU2M2E2NThlZGMxNTBiNDlmODVlNzExNmJjYWExMzpjc180Y2U4MWEyYzJmOTllOGE1OWMxOWU1YTdmMzM4MDQxY2I4YWY4Mzg1")
//					.retrieve()
//					.toBodilessEntity()
//					.map(response -> {
//						if (response.getStatusCode().is2xxSuccessful()) {
//							System.out.println("✅ Update successful!");
//						} else {
//							System.out.println("❌ Failed with status: " + response.getStatusCode());
//						}
//						return response.getStatusCode();
//					})
//					.block();
//
//		};
//
//
//	}

	@Bean
	public WebClient resilientWebClient() {
		HttpClient httpClient = HttpClient.create()
				.responseTimeout(Duration.ofSeconds(5))
				.followRedirect(true)
				.keepAlive(true);

		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.filter(logRequest()) // Log outgoing requests
				.filter(logResponse()) // Log incoming responses
				.defaultHeader("Accept", "application/json")
				.build();
	}

	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			System.out.println("➡️ Request: " + clientRequest.method() + " " + clientRequest.url());
			clientRequest.headers().forEach((name, values) -> values.forEach(value ->
					System.out.println(name + ": " + value)));
			return reactor.core.publisher.Mono.just(clientRequest);
		});
	}

	// Response logging
	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			System.out.println("⬅️ Response Status: " + clientResponse.statusCode());
			return reactor.core.publisher.Mono.just(clientResponse);
		});
	}

	// Retry filter with exponential backoff
	private ExchangeFilterFunction retryFilter() {
		return (request, next) -> next.exchange(request)
				.retryWhen(Retry.backoff(3, Duration.ofMillis(500))
						.filter(throwable -> !(throwable instanceof IllegalArgumentException))
						.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()));
	}

}
