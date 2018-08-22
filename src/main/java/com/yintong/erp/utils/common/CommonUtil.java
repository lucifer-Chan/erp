package com.yintong.erp.utils.common;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-06-07 上午2:29
 **/
public class CommonUtil {

    private static final Charset charset = Charset.forName("UTF-8");

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


    public static Double parseDouble(String s){
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e){
            return null;
        }
    }
}
