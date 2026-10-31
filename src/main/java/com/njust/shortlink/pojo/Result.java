package com.njust.shortlink.pojo;

import lombok.Data;

@Data
// 自动生成 getter、setter、toString、equals 和 hashCode

public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    //无参构造
    public Result() {}

    //成功响应 - 不带数据
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = "success";
        return result;
    }
    //成功响应 - 携带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.data = data;
        return result;
    }

    //失败响应
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.msg = msg;
        return result;
    }
}
