package com.system.exception;

/**
 * @author masonluo
 * @date 2019/11/6 11:43 AM
 */
public class FileAlreadyOpenException extends Exception {
    public FileAlreadyOpenException(){
        this("�ļ��Ѿ���");
    }

    public FileAlreadyOpenException(String msg){
        super(msg);
    }

}
