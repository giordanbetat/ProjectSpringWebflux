package com.giordanbetat.client.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.giordanbetat.client.model.Product;
import com.giordanbetat.client.service.ProductService;

import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

	@Autowired
	private ProductService service;

	public Mono<ServerResponse> findAll(ServerRequest request) {

		return ServerResponse.ok().contentType(APPLICATION_JSON).body(service.findAll(), Product.class);

	}

	public Mono<ServerResponse> findById(ServerRequest request) {

		String id = request.pathVariable("id");

		return errorHandler(
				service.findById(id).flatMap(p -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(p))
						.switchIfEmpty(ServerResponse.notFound().build()));

	}

	public Mono<ServerResponse> save(ServerRequest request) {

		Mono<Product> product = request.bodyToMono(Product.class);

		return product.flatMap(p -> {

			if (p.getCreateAt() == null) {
				p.setCreateAt(new Date());
			}
			return service.save(p);

		}).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
				.contentType(APPLICATION_JSON).bodyValue(p)).onErrorResume(error -> {
					WebClientResponseException errorResponse = (WebClientResponseException) error;

					if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
						return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
								.bodyValue(errorResponse.getResponseBodyAsString());
					}

					return Mono.error(errorResponse);

				});

	}

	public Mono<ServerResponse> edit(ServerRequest request) {

		Mono<Product> product = request.bodyToMono(Product.class);

		String id = request.pathVariable("id");

		return errorHandler(product.flatMap(p -> service.update(p, id)).flatMap(p -> ServerResponse
				.created(URI.create("/api/client/".concat(p.getId()))).contentType(APPLICATION_JSON).bodyValue(p)));

	}

	public Mono<ServerResponse> delete(ServerRequest request) {

		String id = request.pathVariable("id");

		return errorHandler(service.delete(id).then(ServerResponse.noContent().build()));

	}

	public Mono<ServerResponse> upload(ServerRequest request) {

		String id = request.pathVariable("id");

		return errorHandler(request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class).flatMap(file -> service.upload(file, id))
				.flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
						.contentType(APPLICATION_JSON).bodyValue(p)));

	}

	private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {

		return response.onErrorResume(error -> {
			WebClientResponseException errorResponse = (WebClientResponseException) error;
			if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {

				Map<String, Object> body = new HashMap<>();
				body.put("error", "O Produto não existe: ".concat(errorResponse.getMessage()));
				body.put("timestamp", new Date());
				body.put("status", errorResponse.getStatusCode().value());

				return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
			}
			return Mono.error(errorResponse);

		});
	}

}
