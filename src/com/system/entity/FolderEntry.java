package com.system.entity;

import java.util.Arrays;

public class FolderEntry extends Entry {

    public FolderEntry(){

    }

    public FolderEntry(byte[] data){
        name = new String(Arrays.copyOfRange(data, 0,3 ));
        setProperty(data[5]);
        startBlockIndex = data[6];
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
        data[3] = ' ';
        data[4] = ' ';
        data[5] = getPropertyForByte();
        data[6] = (byte) startBlockIndex;
        data[7] = 0;
        return data;
    }
}
