package com.system.entity;

public abstract class Entry {
    // �ļ���
    String name;
    // ֻ���ļ�
    private boolean readOnly = false;
    // ϵͳ�ļ�
    protected boolean system = false;
    // ��ͨ�ļ�
    private boolean normal = false;
    // �ļ�����Ŀ¼
    private boolean folder = false;
    //��ʼ�̷�
    int startBlockIndex;

    /**
     * ������Ҫʵ�������������Entryת����ָ������byte
     * @return һ��8�ֽڵ�byte
     */
    protected abstract byte[] toBytes();
    /**
     * @return ������ת����һ���ֽڵ�byte
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
     * ��byteת���ɲ������͵�����
     * @param num һ��һ�ֽڵ��ļ�����
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
