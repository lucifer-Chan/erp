package com.yintong.erp.utils.common;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.sf.json.JSONObject;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-06-07 上午2:29
 **/
public class CommonUtil {

    private static final Charset charset = Charset.forName("UTF-8");

    private static final DecimalFormat DECIMAL_FORMAT_DOUBLE = new DecimalFormat("#.00");

    /**
     * 不存在时返回默认
     * @param t
     * @param defaultVale
     * @param <T>
     * @return
     */
    public static <T> T ifNotPresent(T t, T defaultVale){
        return Objects.nonNull(t) ? t : defaultVale;
    }

    /**
     * 为空时返回默认
     * @param text
     * @param defaultText
     * @return
     */
    public static String defaultIfEmpty(String text, String defaultText){
        return !StringUtils.isEmpty(text) ? text : defaultText;
    }

    /**
     * collection只能包含0或1个元素，否则抛异常
     * @param collection
     * @param message
     * @param <T>
     * @return
     */
    public static <T> T single(Collection<T> collection, String message){
        if(CollectionUtils.isEmpty(collection)){
            return null;
        }
        Assert.isTrue(collection.size() == 1, message);
        //noinspection unchecked
        return (T)collection.toArray()[0];
    }

    /**
     *
     * @param collection
     * @param <T>
     * @return
     */
    public static  <T> T single(Collection<T> collection){
        return single(collection, "存在脏数据,请联系管理员");
    }

    /**
     * 构造url
     * @param uri
     * @param params
     * @return
     */
    public static String makeURL(String uri, Map<String, String> params){
        if(null == params || params.isEmpty())
            return uri;
        String [] keys = params.keySet().toArray(new String[0]);
        for(int i = 0; i < keys.length; i ++){
            String s = i == 0 ? "?" : "&";
            uri += s + keys[i] + "=" + params.get(keys[i]);
        }
        return uri;
    }

    /**
     * 转换4字节字符
     * @param src
     * @return
     */
    public static String convert4ByteChar(String src) {
        if(StringUtils.isEmpty(src)) return src;

        Map<String, String> dictionary = new HashMap<>();
        byte[] bytes = src.getBytes(charset);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if ((b & 0xf8) == 0xf0) {
                String repSrc = new String(new byte[] { b, bytes[++i],
                        bytes[++i], bytes[++i] }, charset);
                if (dictionary.get(repSrc) == null) {
                    int code = Character.toCodePoint(repSrc.charAt(0),
                            repSrc.charAt(1));
                    dictionary.put(repSrc, String.format("\\u{%X}", code));
                }
            } else if ((b & 0xf0) == 0xe0) {
                i += 2;
            } else if ((b & 0xe0) == 0xc0) {
                i += 1;
            }
        }
        return replace(src, dictionary);
    }


    /**
     * 取消转换4字节字符
     * @param src
     * @return
     */
    public static String unconvert4ByteChar(String src) {
        if(StringUtils.isEmpty(src)) return src;
        Map<String, String> dictionary = new HashMap<>();
        Pattern pattern = Pattern.compile("\\\\u\\{([0-9A-Fa-f]+)\\}");
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            String repSrc = matcher.group(0);
            if (dictionary.get(repSrc) == null) {
                int code = Integer.valueOf(matcher.group(1), 16);
                dictionary.put(repSrc, new String(Character.toChars(code)));
            }
        }
        return replace(src, dictionary);
    }

    private static String replace(String src, Map<String, String> dictionary) {
        if(StringUtils.isEmpty(src) || CollectionUtils.isEmpty(dictionary)){
            return src;
        }

        String dst = src;
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            dst = dst.replace(entry.getKey(), entry.getValue());
        }
        return dst;
    }

    /**
     * 保留两位
     * @param d
     * @return
     */
    public static Double toFixed2(Double d){
        return Double.valueOf(DECIMAL_FORMAT_DOUBLE.format(Objects.isNull(d) ? 0d : d));
    }


    public static Double parseDouble(String s){
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e){
            return null;
        }
    }

    public static Long parseLong(String s){
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e){
            return null;
        }
    }

    public static JSONObject join(JSONObject ... jsons){
        JSONObject ret = new JSONObject();
        Stream.of(jsons).forEach(ret::putAll);
        return ret;
    }

    /**
     * KG -> 只的换算
     * @param product
     * @param kg
     * @return
     */
    public static Integer kg2Num(ErpBaseEndProduct product, double kg){
        //每kg生产的个数
        Double numOneKg = parseDouble(product.getOnlyOrKg());
        Assert.notNull(numOneKg, "请维护 ".concat(product.getDescription()).concat(" 的'只/kg'属性"));
        return new Double(kg * numOneKg).intValue();
    }


    /**
     * num为kg；根据订单单位（kg／只）计算出数量
     * @param product
     * @param unit
     * @param num kg
     * @return
     */
    public static Double calcFromKg(ErpBaseEndProduct product, String unit, double num){
        if(Objects.isNull(product)
                || !StringUtils.hasText(product.getOnlyOrKg())
                || !StringUtils.hasText(unit)
                || !Arrays.asList("kg","只").contains(unit.toLowerCase())){
            return num;
        }

        if("kg".equalsIgnoreCase(unit)) return num;

        //计算个数
        //每kg生产的个数
        Double numOneKg = parseDouble(product.getOnlyOrKg());
        Assert.notNull(numOneKg, "请维护 ".concat(product.getDescription()).concat(" 的'只/kg'属性"));
        return (double) new Double(num * numOneKg).intValue();
    }

    /**
     *
     * @param product
     * @param unit
     * @param num kg or 只
     * @return kg
     */
    public static Double calc2Kg(ErpBaseEndProduct product, String unit, double num){
        if(Objects.isNull(product)
                || !StringUtils.hasText(product.getOnlyOrKg())
                || !StringUtils.hasText(unit)
                || !Arrays.asList("kg","只").contains(unit.toLowerCase())){
            return num;
        }

        if("kg".equalsIgnoreCase(unit)) return num;
        //计算kg
        //每kg生产的个数
        Double numOneKg = parseDouble(product.getOnlyOrKg());
        Assert.notNull(numOneKg, "请维护 ".concat(product.getDescription()).concat(" 的'只/kg'属性"));
        return toFixed2(num / numOneKg);
    }

    public static String toString(Object object){
        return Objects.isNull(object) ? "" : object.toString();
    }

    /**
     * inputStream -> byte[]
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] input2Byte(InputStream inputStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inputStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        return swapStream.toByteArray();
    }
}
