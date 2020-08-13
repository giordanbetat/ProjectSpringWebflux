package com.giordanbetat.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.giordanbetat.client.handler.ProductHandler;

@Configuration
public class RouterConfig {

	@Bean
	public RouterFunction<ServerResponse> route(ProductHandler handler) {
		return RouterFunctions.route(RequestPredicates.GET("/api/client"), handler::findAll)
				.andRoute(RequestPredicates.GET("/api/client/{id}"), handler::findById)
				.andRoute(RequestPredicates.POST("/api/client"), handler::save)
				.andRoute(RequestPredicates.PUT("/api/client/{id}"), handler::edit)
				.andRoute(RequestPredicates.DELETE("/api/client/{id}"), handler::delete)
				.andRoute(RequestPredicates.POST("/api/client/upload/{id}"), handler::upload);
	}

}
