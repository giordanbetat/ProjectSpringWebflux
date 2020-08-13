package com.giordanbetat.project.service;

import com.giordanbetat.project.model.Category;
import com.giordanbetat.project.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductService {

	public Flux<Product> findAll();

	public Flux<Product> findAllByNameUppercase();

	public Flux<Product> findAllByNameUppercaseRepeat();
	
	public Mono<Product> findByName(String name);

	public Mono<Product> findById(String id);

	public Mono<Product> save(Product product);

	public Mono<Void> delete(Product product);

	public Flux<Category> findAllCategories();

	public Mono<Category> findByIdCategory(String id);

	public Mono<Category> saveCategory(Category category);
	
	public Mono<Category> findCategoryByName(String name);

}
