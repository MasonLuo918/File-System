package com.system.openedfiletable;

/* 
 * 已打开文件表中读、写指针的结构
 */
public class Pointer {
	private int dnum; //磁盘盘块号
	private int bnum; //磁盘盘块内第几个字节
	
	public Pointer() {
		// TODO Auto-generated constructor stub
	}

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
