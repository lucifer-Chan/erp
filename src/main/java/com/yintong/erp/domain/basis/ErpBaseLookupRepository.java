package com.yintong.erp.domain.basis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErpBaseLookupRepository extends JpaRepository<ErpBaseLookup, Long> {

    List<ErpBaseLookup> findByTypeOrderByTag(String type);
}
