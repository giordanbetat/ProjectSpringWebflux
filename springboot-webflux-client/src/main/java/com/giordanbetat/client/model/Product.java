package com.giordanbetat.client.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {

	private String id;
	private String name;
	private Double price;
	private Date createAt;
	private String image;
	private Category category;

}
