package com.system.exception;

/**
 * @author masonluo
 * @date 2019/11/6 11:43 AM
 */
public class FileAlreadyOpenException extends Exception {
    public FileAlreadyOpenException(){
        this("文件已经打开");
    }

    public FileAlreadyOpenException(String msg){
        super(msg);
    }

}
