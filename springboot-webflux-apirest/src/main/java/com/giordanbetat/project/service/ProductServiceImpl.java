package com.giordanbetat.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.giordanbetat.project.model.Category;
import com.giordanbetat.project.model.Product;
import com.giordanbetat.project.repository.CategoryRepository;
import com.giordanbetat.project.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements IProductService{

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Override
	public Flux<Product> findAll() {
		
		return productRepository.findAll();
	}

	@Override
	public Flux<Product> findAllByNameUppercase() {
		
		return productRepository.findAll().map(product -> {
			
			product.setName(product.getName().toUpperCase());
			return product;
		});
	}
	
	@Override
	public Flux<Product> findAllByNameUppercaseRepeat() {
		
		return findAllByNameUppercase().repeat(5000);
	}
	
	@Override
	public Mono<Product> findById(String id) {
				
		return productRepository.findById(id);
	}

	@Override
	@Transactional
	public Mono<Product> save(Product product) {
		
		return productRepository.save(product);
	}

	@Override
	public Mono<Void> delete(Product product) {
		
		return productRepository.delete(product);
	}

	@Override
	public Flux<Category> findAllCategories() {
		
		return categoryRepository.findAll();
	}

	@Override
	public Mono<Category> findByIdCategory(String id) {
		return categoryRepository.findById(id);
	}

	@Override
	public Mono<Category> saveCategory(Category category) {
		
		return categoryRepository.save(category);
	}

	@Override
	public Mono<Product> findByName(String name) {
		
		return productRepository.findByName(name);
	}

	@Override
	public Mono<Category> findCategoryByName(String name) {
		
		return categoryRepository.findByName(name);
	}


}
