package org.example.springboot.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "通用返回结果")
public class Result<T> {
    @Schema(description = "状态码", example = "0", required = true)
    private String code;
    
    @Schema(description = "返回信息", example = "成功", required = true)
    private String msg;
    
    @Schema(description = "返回数据")
    private T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Result() {
    }

    public Result(T data) {
        this.data = data;
    }

    public static Result success() {
        Result result = new Result<>();
        result.setCode("0");
        result.setMsg("成功");
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>(data);
        result.setCode("0");
        result.setMsg("成功");
        return result;
    }

    public static <T> Result<T> error(String code, String msg,T data) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
    public static <T> Result<T> error(String code, String msg) {
        return error(code, msg, null);
    }

}
