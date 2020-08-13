package com.giordanbetat.project.controller;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.giordanbetat.project.model.Product;
import com.giordanbetat.project.service.IProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	@Autowired
	private IProductService service;

	@Value("${config.uploads.path}")
	private String path;

	@PostMapping("/v2")
	public Mono<ResponseEntity<Product>> saveImage(Product product, @RequestPart FilePart file) {

		if (product.getCreateAt() == null) {
			product.setCreateAt(new Date());
		}

		product.setImage(UUID.randomUUID().toString() + "-"
				+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));

		return file.transferTo(new File(path + product.getImage())).then(service.save(product))
				.map(p -> ResponseEntity.created(URI.create("/api/products/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(p));
	}

	@PostMapping("/upload/{id}")
	public Mono<ResponseEntity<Product>> upload(@PathVariable(value = "id") String id, @RequestPart FilePart file) {

		return service.findById(id).flatMap(p -> {

			p.setImage(UUID.randomUUID().toString() + "-"
					+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
			return file.transferTo(new File(path + p.getImage())).then(service.save(p));
		}).map(p -> ResponseEntity.ok(p)).defaultIfEmpty(ResponseEntity.notFound().build());

	}

	@GetMapping
	public Mono<ResponseEntity<Flux<Product>>> findAll() {

		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(service.findAll()));
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Product>> findById(@PathVariable(value = "id") String id) {
		return service.findById(id).map(p -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(p))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> save(@Valid @RequestBody Mono<Product> monoProduct) {

		Map<String, Object> response = new HashMap<String, Object>();

		return monoProduct.flatMap(product -> {

			if (product.getCreateAt() == null) {
				product.setCreateAt(new Date());
			}

			return service.save(product).map(p -> {
				
				response.put("product", p);
				response.put("message", "Salvo com sucesso");
				response.put("timestamp", new Date());
				return ResponseEntity.created(URI.create("/api/products/".concat(p.getId())))
					.contentType(MediaType.APPLICATION_JSON).body(response);
			});
			
		}).onErrorResume(t -> {
			return Mono.just(t).cast(WebExchangeBindException.class).flatMap(e -> Mono.just(e.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "O Campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList().flatMap(list -> {
						response.put("errors", list);
						response.put("timestamp", new Date());
						response.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest().body(response));
					});
		});
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<Product>> edit(@RequestBody Product product, @PathVariable(value = "id") String id) {

		return service.findById(id).flatMap(p -> {
			p.setName(product.getName());
			p.setPrice(product.getPrice());
			p.setCategory(product.getCategory());

			return service.save(p);
		}).map(p -> ResponseEntity.created(URI.create("/api/products/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON).body(p)).defaultIfEmpty(ResponseEntity.notFound().build());

	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable(value = "id") String id) {

		return service.findById(id).flatMap(p -> {
			return service.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));

	}

}
