package com.example.common;

/**
 * 统一响应结果（若项目已有可删除此类）
 */
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.setCode("0");
        r.setMsg("成功");
        return r;
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode("0");
        r.setMsg("成功");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(String code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code != null ? code : "-1");
        r.setMsg(msg);
        return r;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
