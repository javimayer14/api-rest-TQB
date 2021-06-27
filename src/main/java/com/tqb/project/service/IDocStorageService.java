package com.tqb.project.service;

import java.util.Optional;

import com.tqb.project.model.Pdf;

public interface IDocStorageService {

	public Pdf getFile(Long fileId);
}
