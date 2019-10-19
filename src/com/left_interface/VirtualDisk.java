package com.left_interface;

/**
 * @author  QTJ
 */
public class VirtualDisk {
    //块号（从1开始）
    private int numberOfBlock;
    //消耗量
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
