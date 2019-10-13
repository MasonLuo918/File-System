package com.left_interface;

public class VirtualDisk {
    private int numberOfBlock;
    private int value;
    public VirtualDisk(int numberOfBlock, int value){
        this.numberOfBlock = numberOfBlock;
        this.value = value;
    }

    public int getNumberOfBlock() {
        return numberOfBlock;
    }

    public void setNumberOfBlock(int numberOfBlock) {
        this.numberOfBlock = numberOfBlock;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
