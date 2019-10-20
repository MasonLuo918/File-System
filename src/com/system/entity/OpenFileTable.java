package com.system.entity;

public class OpenFileTable {
    private String name;

    private byte attribute;

    private int number;
    
    private int length;

    private int flag;

    private Pointer read;

    private Pointer write;

    static class Pointer{
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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

	public void setWrite(Pointer write) {
		this.write = write;
	}
    
}