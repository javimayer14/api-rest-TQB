package com.tqb.project.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tqb.project.model.Pdf;

public interface IPdfDao extends CrudRepository<Pdf, Long> {

	@Query("FROM Pdf p " + "WHERE p.id = :idPdf")
	public Pdf findPdf(@Param("idPdf") Long idUsuario);

}
