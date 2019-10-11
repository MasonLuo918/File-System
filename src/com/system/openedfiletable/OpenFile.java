package com.system.openedfiletable;

import com.sun.istack.internal.NotNull;

/*
 * 已打开文件登记表定义
 */
public class OpenFile {
	private static final int n = 5;
	private OFTLE[] file = new OFTLE[n]; //已打开文件登记表
	private int length; //已打开文件登记表中登记的文件数量
	
	public OpenFile() {
		// TODO Auto-generated constructor stub
		
		this.length = 0;
	}

	public int savefile(OFTLE filename) {
		file[length] = filename;
		length++;
		if(length == n) {
			return 6;
		}else {
			return length;
		}
	}
	
	public OFTLE[] getFile() {
		return file;
	}

	public void setFile(OFTLE[] file) {
		this.file = file;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

}
