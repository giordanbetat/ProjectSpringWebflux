package com.giordanbetat.project.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.giordanbetat.project.model.Category;

import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String>{

	public Mono<Category> findByName(String name);
	
}
