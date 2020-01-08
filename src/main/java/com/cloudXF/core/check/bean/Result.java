package com.cloudXF.core.check.bean;

/**
 * @ClassName: Result
 * @Description: 响应参数
 * @Author: MaoWei
 * @Date: 2020/1/6 16:04
 **/
public class Result<T> {
    /**
     * 状态
     */
    private String status;

    /**
     * 错误码
     */
    private Integer errcode;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 具体的内容
     */
    private T data;

    public String getStatus() {
        return status;
    }

    public Result<T> setStatus(String status) {
        this.status = status;
        return this;
    }

    public Integer getErrcode() {
        return errcode;
    }

    public Result<T> setErrcode(Integer errcode) {
        this.errcode = errcode;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }
}
