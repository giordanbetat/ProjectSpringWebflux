package com.giordanbetat.project;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.giordanbetat.project.model.Category;
import com.giordanbetat.project.model.Product;
import com.giordanbetat.project.service.IProductService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringbootWebfluxApirestApplication implements CommandLineRunner {

	@Autowired
	private IProductService service;

	@Autowired
	private ReactiveMongoTemplate template;

	private static final Logger logger = LoggerFactory.getLogger(SpringbootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringbootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		template.dropCollection("products").subscribe();
		template.dropCollection("categories").subscribe();

		Category eletronico = new Category("Eletronico");
		Category informatica = new Category("Informatica");
		Category esporte = new Category("Esporte");

		Flux.just(eletronico, informatica, esporte).flatMap(service::saveCategory).doOnNext(c -> {
			logger.info("Categoria criada: " + c.getId() + " Nome: " + c.getName());
		}).thenMany(Flux.just(new Product("TV Panasonic 40 pol", 700.00, eletronico),
				new Product("TV Samsung 55 pol", 1500.00, eletronico),
				new Product("TV Philco 28 pol", 400.00, eletronico), new Product("Playstation 5", 3000.00, informatica),
				new Product("PC Gamer", 5000.00, informatica), new Product("Monitor 27 pol", 1299.00, informatica))

				.flatMap(product -> {
					product.setCreateAt(new Date());
					return service.save(product);

				})).subscribe(product -> logger.info("Insert: " + product.getId() + "Name: " + product.getName()));

	}

}