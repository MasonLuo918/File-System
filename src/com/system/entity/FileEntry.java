package com.system.entity;

import java.util.Arrays;

public class FileEntry extends Entry {

    private int length;

    private String type;

    public  FileEntry(){

    }

    public FileEntry(byte[] data){
        name = new String(Arrays.copyOfRange(data, 0,3 ));
        type = new String(Arrays.copyOfRange(data, 3, 5));
        setProperty(data[5]);
        startBlockIndex = data[6];
        length = data[7];
    }

    @Override
    protected byte[] toBytes() {
        byte[] data = new byte[8];
        for(int i = 0; i < data.length; i++){
            data[i] = 0;
        }
        byte[] name = this.name.getBytes();
        for(int i = 0; i < 3; i++){
            if(i < name.length){
                data[i] = name[i];
            }else{
                data[i] = ' ';
            }
        }
        byte[] type = this.type.getBytes();
        for(int i = 0; i < 2; i++){
            if(i < type.length){
                data[3 + i] = type[i];
            }else{
                data[3 + i] = ' ';
            }
        }
        data[5] = getPropertyForByte();
        data[6] = (byte) startBlockIndex;
        data[7] = (byte) length;
        return data;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
