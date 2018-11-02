package com.yintong.erp.utils.excel;

import com.yintong.erp.utils.common.CommonUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;

/**
 * @author lucifer.chan
 * @create 2018-11-02 上午11:33
 * Excel导出
 **/
public class ExcelExporter {

    //属性名所在的行
    private static final int fieldFieldRowAt = 1;
    //属性名所在的列
    private static final int fieldFieldColAt = 0;
    //填值所在的行
    private static final int firstDataRowAt = 2;
    //填充的sheet页
    private static final int sheetAt = 0;
    //格式
    private CellStyle style;
    private XSSFWorkbook excel;
    private XSSFSheet sheet;
    private FormulaEvaluator evaluator;

    private Map<String, Integer> fieldNamesMap = new HashMap<>();

    private List<JSONObject> data;

    /**
     *
     * @param is excel流
     * @param data 数据列表
     * @throws IOException
     */
    public ExcelExporter(InputStream is, List<JSONObject> data) throws IOException {
        this.excel = new XSSFWorkbook(is);
        this.evaluator = excel.getCreationHelper().createFormulaEvaluator();
        this.sheet = excel.getSheetAt(sheetAt);
        this.data = data;
        prepare();
    }

    /**
     * 导出 excel-> bytes
     * @return
     */
    public byte [] export() throws IOException{
        fillRows();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        excel.write(os);
        return os.toByteArray();
    }

    /**
     * 填充
     */
    private void fillRows(){
        if(CollectionUtils.isEmpty(data)) return;
        for(int rowIndex = firstDataRowAt, i =0; rowIndex < data.size(); rowIndex ++ , i ++){
            fillRow(rowIndex, data.get(i));
        }
    }

    /**
     * 将json 填充到row
     * @param rowIndex 行index
     * @param json 具体的一条数据
     */
    private void fillRow(int rowIndex, JSONObject json) {
        Row row = CommonUtil.ifNotPresent(sheet.getRow(rowIndex), sheet.createRow(rowIndex));

        fieldNamesMap.forEach((fieldName, cellIndex) -> {
            String value = CommonUtil.toString(json.get(fieldName));
            fillCell(row, cellIndex, value);
        });
    }

    /**
     * 填充cell
     * @param row
     * @param cellIndex
     * @param value 
     */
    private void fillCell(Row row, int cellIndex, String value){
        Cell cell = CommonUtil.ifNotPresent(row.getCell(cellIndex), row.createCell(cellIndex));
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    private void prepare(){
        prepareStyle();
        XSSFRow fieldRow = sheet.getRow(fieldFieldRowAt);
        for (int i = fieldFieldColAt; i < fieldRow.getLastCellNum(); i ++){
            fieldNamesMap.put(ExcelUtil.getCellValue(evaluator, fieldRow.getCell(i)), i);
        }
    }

    /**
     * 样式
     */
    private void prepareStyle(){
        short black = IndexedColors.BLACK.getIndex();
        style = excel.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN); // 底部边框
        style.setBottomBorderColor(black); // 底部边框颜色
        style.setBorderLeft(BorderStyle.THIN); // 左边边框
        style.setLeftBorderColor(black); // 左边边框颜色
        style.setBorderRight(BorderStyle.THIN); // 右边边框
        style.setRightBorderColor(black); // 右边边框颜色
        style.setBorderTop(BorderStyle.THIN); // 上边边框
        style.setTopBorderColor(black); // 上边边框颜色
    }
}
