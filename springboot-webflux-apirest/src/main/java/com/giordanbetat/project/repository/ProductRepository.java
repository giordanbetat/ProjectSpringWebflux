package com.giordanbetat.project.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.giordanbetat.project.model.Product;

import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String>{

	public Mono<Product> findByName(String name);
	
	@Query("{ 'name': ?0 }")
	public Mono<Product> searchByName(String name);
	
}
