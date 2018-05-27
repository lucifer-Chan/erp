package com.yintong.erp.utils.excel;

import lombok.Getter;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedFunction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lucifer.chan
 * @create 2018-05-27 上午9:10
 * excel辅助类
 **/
public class ExcelUtil {

    private static final int fieldRowAt = 1;
    private static final int firstDataRowAt = 2;
    private XSSFWorkbook wb;
    private XSSFSheet sheet;
    private FormulaEvaluator evaluator;
    private List<List<String>> data;
    private List<String> fieldNames;

    public ExcelUtil(InputStream is) throws IOException {
        data = new ArrayList<>();
        fieldNames = new ArrayList<>();
        wb = new XSSFWorkbook(is);
        evaluator = wb.getCreationHelper().createFormulaEvaluator();
        sheet = wb.getSheetAt(0);
        prepare();
    }

    public <T extends Importable> ExcelImporter<T> builder(Class<T> clazz){
        return new ExcelImporter<>(this, clazz);
    }

    public static class ExcelImporter<T extends Importable> {
        private ExcelUtil excelUtil;
        @Getter
        private List<T> successData = new ArrayList<>();
        @Getter
        private List<List<String>> errorData = new ArrayList<>();

        private Class<T> clazz;

        private ExcelImporter(ExcelUtil excelUtil, Class<T> clazz){
            this.excelUtil = excelUtil;
            this.clazz = clazz;
            wrapper();
        }

        private void wrapper(){
            for (List<String> row : excelUtil.data){
                //noinspection unchecked
                T entity = (T)Unchecked.function((CheckedFunction<Class, Object>) Class::newInstance).apply(clazz);
                try{
                    entity.assign(row, excelUtil.fieldNames);
                    successData.add(entity);
                } catch (Exception e){
                    //noinspection unchecked
                    row.add(e.getMessage());
                    errorData.add(row);
                }
            }
        }
    }

    private void prepare(){
        XSSFRow fieldRow = sheet.getRow(fieldRowAt);
        int cellStartAt = fieldRow.getFirstCellNum();
        int cellEndAt = fieldRow.getLastCellNum();
        for (int i = cellStartAt; i < cellEndAt; i ++){
            fieldNames.add(getCellValue(fieldRow.getCell(i)));
        }

        for (int rowNum = firstDataRowAt; rowNum <= sheet.getLastRowNum(); rowNum++) {
            XSSFRow row = sheet.getRow(rowNum);
            List<String> rowData = new ArrayList<>();
            for (int i = cellStartAt; i < cellEndAt; i ++){
                rowData.add(getCellValue(row.getCell(i)));
            }
            data.add(rowData);
        }
    }

    private String getCellValue(XSSFCell cell) {
        if (null == cell)
            return null;
        String ret;
        switch (cell.getCellTypeEnum()) {
            case _NONE:
                ret = null;
                break;
            case FORMULA:
                CellValue cellValue = evaluator.evaluate(cell);
                switch (cellValue.getCellTypeEnum()){
                    case _NONE:
                        ret = null;
                        break;
                    case STRING:
                    default:
                        ret = cellValue.getStringValue();
                        break;
                }
                break;
            case STRING :
            default:
                ret = cell.getStringCellValue();
                break;
        }
        return ret;
    }
}
