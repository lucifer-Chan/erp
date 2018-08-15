package com.yintong.erp.utils.common;

import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.Key;

/**
 * @author lucifer.chan
 * @create 2018-08-13 下午8:54
 * 加密
 **/
public class AESUtil {
    private static final Charset utf8 = Charset.forName("UTF-8");
    private static final String DEFAULT_KEY = "3.14159265358979";
    private static final String iv = "3238462643383279";

    private Key keySpec;
    private IvParameterSpec ivSpec;
    private Cipher cipher;

    private AESUtil(){
        keySpec = new SecretKeySpec(DEFAULT_KEY.getBytes(), "AES");
        ivSpec = new IvParameterSpec(iv.getBytes());
        try{
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch(Exception ignored){}
    }

    private static class AESUtilHolder{
        private static AESUtil instance = new AESUtil();
    }

    public static AESUtil getInstance(){
        return AESUtilHolder.instance;
    }

    /**
     * 加密
     * @param source
     * @return
     */
    public String encrypt(String source){
        try{
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] b = cipher.doFinal(source.getBytes());
            return new String(Hex.encode(b), utf8);
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密
     * @param source
     * @return
     */
    public String decrypt(String source) {
        try{
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] bytes = Hex.decode(source);
            byte[] ret = cipher.doFinal(bytes);
            return new String(ret, utf8);
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}