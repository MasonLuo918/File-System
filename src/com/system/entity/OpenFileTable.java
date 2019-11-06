package com.system.entity;

public class OpenFileTable {

    private FileEntry fileEntry;

    private String name;

    private byte attribute;

    private int startBlockNum;
    
    private int length;

    private int flag;

    private Pointer read;

    private Pointer write;

    public OpenFileTable() {
    }

    public OpenFileTable(FileEntry fileEntry, String name, byte attribute, int startBlockNum, int length, int flag) {
        this.fileEntry = fileEntry;
        this.name = name;
        this.attribute = attribute;
        this.startBlockNum = startBlockNum;
        this.length = length;
        this.flag = flag;
    }

    public static class Pointer{
        private int dnum;

        private int bnum;

        public int getDnum() {
            return dnum;
        }

        public void setDnum(int dnum) {
            this.dnum = dnum;
        }

        public int getBnum() {
            return bnum;
        }

        public void setBnum(int bnum) {
            this.bnum = bnum;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getAttribute() {
        return attribute;
    }

    public void setAttribute(byte attribute) {
        this.attribute = attribute;
    }

    public int getStartBlockNum() {
        return startBlockNum;
    }

    public void setStartBlockNum(int startBlockNum) {
        this.startBlockNum = startBlockNum;
    }

    public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

	public Pointer getRead() {
		return read;
	}

	public void setRead(Pointer read) {
		this.read = read;
	}

	public Pointer getWrite() {
		return write;
	}

    public FileEntry getFileEntry() {
        return fileEntry;
    }

    public void setFileEntry(FileEntry fileEntry) {
        this.fileEntry = fileEntry;
    }

    public void setWrite(Pointer write) {
		this.write = write;
	}
    
}