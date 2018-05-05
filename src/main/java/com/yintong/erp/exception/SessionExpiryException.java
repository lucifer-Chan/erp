package com.yintong.erp.exception;

import java.util.Objects;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午4:11
 * 会话丢失异常
 **/
public class SessionExpiryException extends RuntimeException{
    public SessionExpiryException(String s) {
        super(s);
    }
    private static final long serialVersionUID = -999999999999999999L;

    public static void notNull(Object object, String message){
        if(Objects.isNull(object))
            throw new SessionExpiryException(message);
    }
}
