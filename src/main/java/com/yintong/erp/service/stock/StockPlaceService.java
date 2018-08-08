package com.yintong.erp.service.stock;

import com.yintong.erp.domain.stock.ErpStockPlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lucifer.chan
 * @create 2018-08-04 下午9:53
 * 仓位服务
 **/
@Service
public class StockPlaceService {
    @Autowired ErpStockPlaceRepository stockPlaceRepository;


}
