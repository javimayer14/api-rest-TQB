package com.tqb.project.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.tqb.project.dao.IPdfDao;
import com.tqb.project.model.Pdf;

@Service
public class DocStorageService implements IDocStorageService {
	@Autowired
	private IPdfDao pdfDao;

	public Pdf saveFile(MultipartFile file) {
		//String docName = file.getOriginalFilename();
		try {
			Pdf pdf = new Pdf("prueba",file.getContentType(), file.getBytes());
			return pdfDao.save(pdf);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error");
		}
	}

	  public Pdf getFile(Long fileId) {
		  return pdfDao.findPdf(fileId);
	  }


}
