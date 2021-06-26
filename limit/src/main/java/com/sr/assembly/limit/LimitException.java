package com.sr.assembly.limit;

/**
 * @description: 限流自定义异常
 * @author: sunrui
 * @create: 2021/6/9 7:48 下午
 **/
public class LimitException extends RuntimeException{
    /**
     * 空构造函数
     */
    public LimitException() {
        super();
    }

    /**
     * message构造函数
     * @param message
     */
    public LimitException(String message) {
        super(message);
    }

}
