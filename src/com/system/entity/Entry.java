package com.system.entity;

public abstract class Entry {
    // 文件名
    String name;
    // 只读文件
    private boolean readOnly = false;
    // 系统文件
    protected boolean system = false;
    // 普通文件
    private boolean normal = false;
    // 文件或者目录
    private boolean folder = false;
    //起始盘符
    int startBlockIndex;

    /**
     * 子类需要实现这个方法，将Entry转换成指定规格的byte
     * @return 一个8字节的byte
     */
    protected abstract byte[] toBytes();
    /**
     * @return 将属性转换成一个字节的byte
     */
    byte getPropertyForByte(){
        byte num = 0;
        if(readOnly){
            num |= 1;
        }
        if(system){
            num |= 2;
        }
        if(normal){
            num |= 4;
        }
        if(folder){
            num |= 8;
        }
        return num;
    }

    /**
     * 将byte转换成布尔类型的属性
     * @param num 一个一字节的文件属性
     */
    void setProperty(byte num){
        if((num&1) == 0){
            readOnly = true;
        }
        if((num&2) == 0){
            system = true;
        }
        if((num&4) == 0){
            normal = true;
        }
        if((num&8) == 0){
            folder = true;
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public boolean isNormal() {
        return normal;
    }

    public void setNormal(boolean normal) {
        this.normal = normal;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public int getStartBlockIndex() {
        return startBlockIndex;
    }

    public void setStartBlockIndex(int startBlockIndex) {
        this.startBlockIndex = startBlockIndex;
    }
}
