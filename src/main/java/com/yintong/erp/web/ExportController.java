package com.yintong.erp.web;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.excel.ExcelExporter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lucifer.chan
 * @create 2018-11-02 上午10:45
 * 导出Excel
 **/
@Controller
@RequestMapping("export")
public class ExportController {

    @Autowired ErpBaseEndProductRepository productRepository;

    @Autowired ErpBaseModelToolRepository mouldRepository;


    /**
     * 导出成品数据
     * @param request
     * @param response
     */
    @GetMapping("product")
    public ResponseEntity<byte[]> exportProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream is = ExportController.class.getResourceAsStream("/export_product.xlsx");

        List<JSONObject> data = productRepository.findAllByOrderById().stream()
                .map(ErpBaseEndProduct::toJSONObject)
                .collect(Collectors.toList());

        byte[] bytes = new ExcelExporter(is, data).export();

        String fileName = "成品" + DateUtil.getDateString(new Date()).concat(".xlsx");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(fileName, new String(fileName.getBytes("UTF-8"),"iso-8859-1"));
        return new ResponseEntity<>(bytes, headers, HttpStatus.CREATED);

    }


    /**
     * 导出模具数据
     * @param request
     * @param response
     */
    @GetMapping("mould")
    public ResponseEntity<byte[]> exportMould(HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream is = ExportController.class.getResourceAsStream("/export_mould.xlsx");

        List<JSONObject> data = mouldRepository.findAllByOrderById().stream()
                .map(ErpBaseModelTool::toJSONObject)
                .collect(Collectors.toList());

        byte[] bytes = new ExcelExporter(is, data).export();

        String fileName = "模具" + DateUtil.getDateString(new Date()).concat(".xlsx");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(fileName, new String(fileName.getBytes("UTF-8"),"iso-8859-1"));
        return new ResponseEntity<>(bytes, headers, HttpStatus.CREATED);

    }
}
