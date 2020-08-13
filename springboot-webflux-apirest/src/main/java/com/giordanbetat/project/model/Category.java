package com.giordanbetat.project.model;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Document(collection = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

	@Id
	@NotEmpty
	private String id;
	
	private String name;
	
	public Category(String name) {
		this.name = name;
	}
	
}
