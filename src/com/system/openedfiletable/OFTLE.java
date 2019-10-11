package com.system.openedfiletable;

/*
 * 已打开文件表项类型定义
 */
public class OFTLE {
	private String name; //文件绝对路径名
	private char attribute; //文件的属性，用1个字节表示
	private int number; //文件起始盘块号
	private int length; //文件长度，文件占用的字节数
	private int flag; //操作类型，用“0”表示以读操作方式打开文件，用“1”表示以写操作方式打开文件
	private Pointer read = new Pointer(); //读文件的位置，文件打开时dnum为文件起始盘块号，bnum为“0”
	private Pointer write = new Pointer(); //写文件的位置，文件刚建立时dnum为文件起始盘块号，bnum为“0”，打开文件时dnum和bnum为文件的末尾位置
	
	public OFTLE(String name, Boolean attribute, int number) {
		this.name = name;
		if(attribute == true) {
			this.attribute = '1';
		}else {
			this.attribute = '0';
		}
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public char getAttribute() {
		return attribute;
	}

	public void setAttribute(char attribute) {
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
