package com.system.exception;

public class OutOfLengthException extends Exception {

    public OutOfLengthException(){

    }

    public OutOfLengthException(String string){
        super(string);
    }
}
