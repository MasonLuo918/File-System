package com.system.exception;

public class DiskCapacityErrorException extends Exception {
    public DiskCapacityErrorException(){
        super("Load disk error");
    }
}
