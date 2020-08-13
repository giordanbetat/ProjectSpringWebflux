package com.giordanbetat.project;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giordanbetat.project.model.Category;
import com.giordanbetat.project.model.Product;
import com.giordanbetat.project.service.IProductService;

import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SpringbootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private IProductService service;
	
	@Value("${config.base.endpoint}")
	private String url;
	
	@Test
	public void findAll() {
		
		client.get()
		.uri(url)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Product.class)
		.consumeWith(response -> {
			List<Product> products = response.getResponseBody();
			products.forEach(p -> {
				System.out.println(p.getName());
			});
			
			Assertions.assertThat(products.size() > 0).isTrue();
			
		})
		;
		
	}
	
	@Test
	public void findById() {
		
		String nameProduct = "TV Panasonic 40 pol";
		
		Product product = service.findByName(nameProduct).block();
				
		client.get()
		.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Product.class)
		.consumeWith(response -> {
			
			Product productResponse = response.getResponseBody();
						
			Assertions.assertThat(productResponse.getId()).isNotEmpty();
			Assertions.assertThat(productResponse.getName()).isEqualTo(nameProduct);
			
		})
		/*
		 * .expectBody() .jsonPath("$.id").isNotEmpty()
		 * .jsonPath("$.name").isEqualTo(nameProduct)
		 */
		;

	}
	
	
	@Test
	public void save() {
		
		Category category = service.findCategoryByName("Eletronico").block();
		
		Product product = new Product("Moto Z3", 1000.00, category);
		
		client.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(product), Product.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.product.id").isNotEmpty()
		.jsonPath("$.product.name").isEqualTo(product.getName())
		.jsonPath("$.product.category.name", category.getName())
		;
		
	}
	
	@Test
	public void save2() {
		
		Category category = service.findCategoryByName("Eletronico").block();
		
		Product product = new Product("Moto Z3", 1000.00, category);
		
		client.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(product), Product.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
		.consumeWith(response -> {
			
			Object object = response.getResponseBody().get("product");
			Product productResponse = new ObjectMapper().convertValue(object, Product.class);
			Assertions.assertThat(productResponse.getId()).isNotEmpty();
			Assertions.assertThat(productResponse.getName()).isEqualTo(product.getName());
			Assertions.assertThat(productResponse.getCategory().getName()).isEqualTo(product.getCategory().getName());
			
		});
		
	}
	
	@Test
	public void edit() {
		
		Product product = service.findByName("PC Gamer").block();
		Category category = service.findCategoryByName("Eletronico").block();
		
		Product productEdit = new Product("PC Gamer TOP", 700.00, category);
		
		client.put().uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(productEdit), Product.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.name").isEqualTo("PC Gamer TOP")
		.jsonPath("$.category.name").isEqualTo("Eletronico");
		
	}
	
	@Test
	public void delete() {
		Product product = service.findByName("Monitor 27 pol").block();
		client.delete()
		.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.exchange()
		.expectStatus().isNoContent()
		.expectBody()
		.isEmpty();
		
		client.get()
		.uri(url + "/{id}", Collections.singletonMap("id", product.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody()
		.isEmpty();
	}
	
	
}
