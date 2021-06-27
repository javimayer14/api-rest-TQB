package com.tqb.project.model.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class PresentProjectDTO {
	
	private String name;
	private String lastName;
	private MultipartFile file;
	private String desc;

}
