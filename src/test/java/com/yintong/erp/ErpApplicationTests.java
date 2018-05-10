package com.yintong.erp;

import com.yintong.erp.domain.basis.warehouse.ErpBaseWarehouse;
import com.yintong.erp.domain.basis.warehouse.ErpBaseWarehouseRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ErpApplicationTests {

	@Autowired
	private ErpBaseWarehouseRepository erpBaseWarehouseRepository;

	@Test
	public void contextLoads() {
	}

	@Ignore
	@Test
	public void entityTest1(){
		ErpBaseWarehouse erpBaseWarehouse = new ErpBaseWarehouse();
		erpBaseWarehouse.setAssetNo("00001");
		erpBaseWarehouseRepository.save(erpBaseWarehouse);

	}

	@Test
	public void entityTest2(){

	}

}
