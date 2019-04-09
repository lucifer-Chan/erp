package com.yintong.erp.exception;

import com.aliyuncs.exceptions.ClientException;
import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.base.JsonWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.NonUniqueResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午4:03
 * 全局异常处理
 **/
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${yintong.erp.model.debug}")
    private boolean debug;

    @Override
    protected final ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers, HttpStatus status, WebRequest webRequest){
        BaseResult ret;
        if(webRequest instanceof ServletWebRequest) {
            HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
            HttpServletResponse response = ((ServletWebRequest) webRequest).getResponse();

            int code =
                    Objects.nonNull(status) ? status.value() :
                            body instanceof Body && Objects.nonNull(((Body) body).getStatus()) ?
                                    ((Body) body).getStatus().value() : HttpStatus.INTERNAL_SERVER_ERROR.value();

            if(Objects.nonNull(headers)){
                headers.toSingleValueMap().forEach(response::setHeader);
            }
            ret = convertException(request, response, body, code, e);
        } else{
            ret = convertException(webRequest.getParameterMap(), headers.toSingleValueMap(), status, e);
        }
        //状态码依旧为200，不去改变！
        return new ResponseEntity<>(ret, headers, HttpStatus.OK);
    }

    protected ResponseEntity<Object> handleExtensionException(Exception e, ExtensionStatus ExtensionStatus, String errorMsg, ServletWebRequest request){
        return this.handleExceptionInternal(e, new Body(ExtensionStatus, errorMsg), null, null, request);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class, MultipartException.class, EmptyResultDataAccessException.class, NonUniqueResultException.class, SessionExpiryException.class})
    @ResponseBody
    public final ResponseEntity<Object> handleExtensionException(Exception e, ServletWebRequest request){
        if(e instanceof IllegalArgumentException){
            return handleIllegalArgumentException((IllegalArgumentException)e, new Body(ExtensionStatus.ILLEGAL_ARGUMENT, e.getMessage()), request);
        } else if(e instanceof MultipartException){
            return handleMultipartException((MultipartException)e, new Body(ExtensionStatus.MULTIPART_EXCEPTION), request);
        } else if(e instanceof EmptyResultDataAccessException){
            return handleEmptyResultDataAccessException((EmptyResultDataAccessException)e, new Body(ExtensionStatus.EMPTY_RESULT_DATA, e.getMessage()), request);
        } else if(e instanceof NonUniqueResultException){
            return handleNonUniqueResultException((NonUniqueResultException)e, new Body(ExtensionStatus.NON_UNIQUE_RESULT, e.getMessage()), request);
        } else if(e instanceof ClientException) {
            return handleClientException((ClientException)e, new Body(ExtensionStatus.SMS_FAILED), request);
        } else if (e instanceof  SessionExpiryException){
            return handleSessionExpiryException((SessionExpiryException)e, new Body(ExtensionStatus.SESSION_EXPIRY, e.getMessage()), request);
        }

        else {
            return this.handleExceptionInternal(e, new Body(ExtensionStatus.UN_KNOW_STATUS, e.getMessage()), null, null, request);
        }

    }

    /**
     * 会话丢失异常
     * @param e
     * @param body
     * @param request
     * @return
     */
    protected ResponseEntity<Object> handleSessionExpiryException(SessionExpiryException e, Body body, ServletWebRequest request) {
        return this.handleExceptionInternal(e, body, null, null, request);
    }

    /**
     * 参数错误
     * @param e
     * @param body
     * @param request
     * @return
     */
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e, Body body, ServletWebRequest request){
        return this.handleExceptionInternal(e, body, null, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * 文件上传失败
     * @param e
     * @param body
     * @param request
     * @return
     */
    protected ResponseEntity<Object> handleMultipartException(MultipartException e, Body body, ServletWebRequest request){
        return this.handleExceptionInternal(e, body, null, HttpStatus.BAD_REQUEST, request);
    }

    protected ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException e, Body body, ServletWebRequest request){
        return this.handleExceptionInternal(e, body, null, null, request);
    }

    protected ResponseEntity<Object> handleNonUniqueResultException(NonUniqueResultException e, Body body, ServletWebRequest request){
        return this.handleExceptionInternal(e, body, null, null, request);
    }

    /**
     * 短信发送失败
     * @param e
     * @param body
     * @param request
     * @return
     */
    protected ResponseEntity<Object> handleClientException(ClientException e, Body body, ServletWebRequest request) {
        return this.handleExceptionInternal(e, body, null, null, request);
    }

    /**
     * 未定义的异常
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<Object> handleOtherException(Exception e, ServletWebRequest request){
        return handleExceptionInternal(e, request, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * 当 WebRequest不是ServletWebRequest的时候起作用
     * @param params
     * @param headers
     * @param status
     * @param e
     * @return
     */
    protected BaseResult convertException(Map<String, String[]> params, Map<String, String> headers, HttpStatus status, Exception e){
        String errorMsg = e.getMessage();
        if(!StringUtils.hasLength(errorMsg))
            errorMsg = status.getReasonPhrase();
        if(errorMsg.contains(";")){
            errorMsg = errorMsg.split(";")[0];
        }
        BaseResult ret = new BaseResult(status, errorMsg);
        if(debug){
            ret.put("request", JsonWrapper.builder()
                    .add("params", params)
                    .build()
            )
            .put("response", JsonWrapper.builder()
                    .add("status", status(status.value()))
                    .add("headers", headers)
                    .build()
            )
            .put("error", error(e)
            );
            log.error("异常\t{}", ret);
        }
        log.error("error\t{}", e);
        return ret;
    }


    protected BaseResult convertException(HttpServletRequest request, HttpServletResponse response, Object body, int code, Exception e){
        int status = code == 0 ? response.getStatus() : code;
        String errorMsg = e.getMessage();
        if(Objects.nonNull(body) && body instanceof Body)
            errorMsg = ((Body) body).getErrorMsg();
        if(!StringUtils.hasLength(errorMsg)){
            try{
                errorMsg = HttpStatus.valueOf(status).getReasonPhrase();
            } catch (IllegalArgumentException ex){
                errorMsg = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
            }
        }
        if(errorMsg.contains(";")){
            errorMsg = errorMsg.split(";")[0];
        }
        BaseResult ret = new BaseResult(status, errorMsg);
        if(debug){
            ret.put("request", JsonWrapper.builder()
                    .add("params", request.getParameterMap())
                    .add("method", request.getMethod())
                    .add("from", request.getRequestURL().toString())
                    .build()
            )
            .put("response", JsonWrapper.builder()
                    .add("status", status(status))
                    .add("headers", response.getHeaderNames().stream().collect(Collectors.toMap(Function.identity(), response::getHeader)))
                    .build()
            )
            .put("error", error(e)
            );
            log.error("异常\t{}", ret);
        }
        log.error("error\t{}", e);
        return ret;
    }

    /**
     * 异常的StackTrace过滤的包
     * @return
     */
    protected String [] filterStackTracePackage(){
        return new String[]{"com.yintong.erp"};
    }

    private JSONObject status(int value){
        String reasonPhrase;

        try{
            reasonPhrase = HttpStatus.valueOf(value).getReasonPhrase();
        } catch(IllegalArgumentException e){
            reasonPhrase = ExtensionStatus.valueOf(value).getReasonPhrase();
        }
        return JsonWrapper.builder().add("code", value).add("name", reasonPhrase).build();
    }

    private JSONObject error(Exception e){
        return JsonWrapper.builder()
                .add("cause", Objects.isNull(e.getCause()) ? e.getClass().getName() : e.getCause().getClass().getName())
                .add("message", e.getMessage())
                .add("detail", e.toString())
                .add("stack", Stream.of(e.getStackTrace()).filter(filter).collect(Collectors.toList()))
                .build();
    }

    private Predicate<StackTraceElement> filter = (e) -> {
        String [] packages = filterStackTracePackage();
        if(Objects.isNull(packages))
            packages = new String[0];
        return Stream.of(packages).anyMatch(className->e.getClassName().startsWith(className));
    };

    @Getter
    public static class Body{
        private ExtensionStatus status;
        private String errorMsg;

        public Body(ExtensionStatus status, String errorMsg){
            this.status = status;
            this.errorMsg = errorMsg;
        }

        public Body(ExtensionStatus status){
            this.status = status;
            this.errorMsg = status.reasonPhrase;
        }

        public Body(String errorMsg){
            this.status = null;
            this.errorMsg = errorMsg;
        }
    }

    @Getter
    public enum ExtensionStatus {
        ILLEGAL_ARGUMENT(1001, "参数异常"),
        MULTIPART_EXCEPTION(1002, "文件上传失败"),
        EMPTY_RESULT_DATA(1003, "Empty Result"),
        NON_UNIQUE_RESULT(1004, "NonUniqueResult"),
        AUTHENTICATION_FAILURE(1005, "认证失败"),
        SMS_FAILED(2001,"短息发送失败"),
        UN_KNOW_STATUS(-1, "未知状态"),
        SESSION_EXPIRY(999, "会话过期");

        private final int value;
        private final String reasonPhrase;

        ExtensionStatus(int value, String reasonPhrase) {
            this.value = value;
            this.reasonPhrase = reasonPhrase;
        }

        public int value(){
            return this.value;
        }

        public static ExtensionStatus valueOf(int statusCode) {
            return Stream.of(values()).filter(status -> statusCode == status.value).findAny().orElse(UN_KNOW_STATUS);
        }
    }
}