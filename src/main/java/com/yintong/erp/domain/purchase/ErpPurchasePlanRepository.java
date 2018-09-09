package com.yintong.erp.domain.purchase;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpPurchasePlanRepository extends JpaRepository<ErpPurchasePlan, Long> {

    /**
     *
     * @param waresId
     * @param waresType
     * @param start
     * @param end
     * @return
     */
    List<ErpPurchasePlan> findByWaresIdAndWaresTypeAndCreatedAtIsBetween(Long waresId, String waresType, Date start, Date end);


    List<ErpPurchasePlan> findByWaresIdAndWaresType(Long waresId, String waresType);

    /**
     * 模糊查询
     * @param specification
     * @param pageable
     * @return
     */
    Page<ErpPurchasePlan> findAll(Specification<ErpPurchasePlan> specification, Pageable pageable);


}
