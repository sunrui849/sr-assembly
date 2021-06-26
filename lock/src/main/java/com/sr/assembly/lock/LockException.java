package com.sr.assembly.lock;

/**
 * @description: redis锁自定义异常
 * @author: sunrui
 * @create: 2021/6/23 11:50 上午
 **/
public class LockException extends RuntimeException{

    /**
     * 空构造函数
     */
    public LockException() {
        super();
    }

    /**
     * message构造函数
     * @param message
     */
    public LockException(String message) {
        super(message);
    }

    /**
     * 异常和message构造函数
     * @param message
     * @param cause
     */
    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 异常构造函数
     * @param cause
     */
    public LockException(Throwable cause) {
        super(cause);
    }

}
