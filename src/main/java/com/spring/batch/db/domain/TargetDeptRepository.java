package com.spring.batch.db.domain;

import org.springframework.data.repository.CrudRepository;

public interface TargetDeptRepository extends CrudRepository<Dept, Long> {
}
