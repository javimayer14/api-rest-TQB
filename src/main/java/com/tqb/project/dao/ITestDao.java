package com.tqb.project.dao;

import org.springframework.data.repository.CrudRepository;

import com.tqb.project.model.TestResult;

public interface ITestDao extends CrudRepository<TestResult, Long>{

}



