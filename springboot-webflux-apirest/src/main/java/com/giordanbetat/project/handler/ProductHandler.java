package com.giordanbetat.project.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.giordanbetat.project.model.Category;
import com.giordanbetat.project.model.Product;
import com.giordanbetat.project.service.IProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

	@Autowired
	private IProductService service;

	@Value("${config.uploads.path}")
	private String path;

	@Autowired
	private Validator validator;

	public Mono<ServerResponse> saveWithImage(ServerRequest request) {

		Mono<Product> product = request.multipartData().map(multipart -> {
			FormFieldPart name = (FormFieldPart) multipart.toSingleValueMap().get("name");
			FormFieldPart price = (FormFieldPart) multipart.toSingleValueMap().get("price");
			FormFieldPart categoryId = (FormFieldPart) multipart.toSingleValueMap().get("category.id");
			FormFieldPart categoryName = (FormFieldPart) multipart.toSingleValueMap().get("category.name");

			Category category = new Category(categoryName.value());
			category.setId(categoryId.value());

			return new Product(name.value(), Double.parseDouble(price.value()), category);

		});

		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file")).cast(FilePart.class)
				.flatMap(file -> product.flatMap(p -> {
					p.setImage(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
					p.setCreateAt(new Date());
					return file.transferTo(new File(path + p.getImage())).then(service.save(p));
				})).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(fromValue(p)));
	}

	public Mono<ServerResponse> upload(ServerRequest request) {

		String id = request.pathVariable("id");

		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file")).cast(FilePart.class)
				.flatMap(file -> service.findById(id).flatMap(p -> {
					p.setImage(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
					return file.transferTo(new File(path + p.getImage())).then(service.save(p));
				}))
				.flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(fromValue(p)))
				.switchIfEmpty(ServerResponse.notFound().build());

	}

	public Mono<ServerResponse> findAll(ServerRequest request) {

		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(service.findAll(), Product.class);
	}

	public Mono<ServerResponse> findById(ServerRequest request) {

		String id = request.pathVariable("id");

		return service.findById(id)
				.flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(p)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> save(ServerRequest request) {

		Mono<Product> product = request.bodyToMono(Product.class);

		return product.flatMap(p -> {

			Errors errors = new BeanPropertyBindingResult(p, Product.class.getName());
			validator.validate(p, errors);

			if (errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "O campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
						.collectList().flatMap(list -> ServerResponse.badRequest().body(fromValue(list)));
			} else {
				if (p.getCreateAt() == null) {
					p.setCreateAt(new Date());
				}

				return service.save(p)
						.flatMap(pDb -> ServerResponse.created(URI.create("/api/v2/products/".concat(pDb.getId())))
								.contentType(MediaType.APPLICATION_JSON).body(fromValue(pDb)));
			}

		});

	}

	public Mono<ServerResponse> edit(ServerRequest request) {

		Mono<Product> product = request.bodyToMono(Product.class);

		String id = request.pathVariable("id");

		Mono<Product> productDb = service.findById(id);

		return productDb.zipWith(product, (db, req) -> {
			db.setName(req.getName());
			db.setPrice(req.getPrice());
			db.setCategory(req.getCategory());
			return db;
		}).flatMap(p -> ServerResponse.created(URI.create("/api/v2/products/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON).body(service.save(p), Product.class))
				.switchIfEmpty(ServerResponse.notFound().build());

	}

	public Mono<ServerResponse> delete(ServerRequest request) {

		String id = request.pathVariable("id");

		Mono<Product> productDb = service.findById(id);

		return productDb.flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());

	}

}
