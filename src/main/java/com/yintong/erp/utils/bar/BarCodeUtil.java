package com.yintong.erp.utils.bar;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

/**
 * @author lucifer.chan
 * @create 2018-07-24 下午2:39
 * 条形码生成工具
 **/
public class BarCodeUtil {

    /**
     * 生成文件
     * @param barcode
     * @param path
     * @return
     */
    public static File generateFile(String barcode, String path) {
        File file = new File(path);
        try {
            generate(barcode, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    /**
     * 生成字节
     * @param barcode
     * @return
     */
    public static byte[] generate(String barcode) {
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        generate(barcode, ous);
        return ous.toByteArray();
    }

    /**
     * 生成到流
     *
     * @param barcode
     * @param out
     */
    public static void generate(String barcode, OutputStream out) {
        if (StringUtils.isEmpty(barcode) || out == null) {
            return;
        }

        Code39Bean bean = new Code39Bean();

        // 精细度
        final int dpi = 500;
        // module宽度
        final double moduleWidth = UnitConv.in2mm(1.0f / 150);

        // 配置对象
        bean.setModuleWidth(moduleWidth);
        bean.setWideFactor(3);
        bean.doQuietZone(false);

        String format = "image/png";
        try {

            // 输出到流
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, format, dpi,
                    BufferedImage.TYPE_BYTE_BINARY, false, 0);
            // 生成条形码
            bean.generateBarcode(canvas, barcode);
            // 结束绘制
            canvas.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String msg = "PTD0000000096";
        String path = "/Users/lucifer.chan/Desktop/" + msg + ".png";
        generateFile(msg, path);
    }
}
