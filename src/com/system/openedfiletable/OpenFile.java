package com.system.openedfiletable;

import com.sun.istack.internal.NotNull;

/*
 * �Ѵ��ļ��ǼǱ���
 */
public class OpenFile {
	private static final int n = 5;
	private OFTLE[] file = new OFTLE[n]; //�Ѵ��ļ��ǼǱ�
	private int length; //�Ѵ��ļ��ǼǱ��еǼǵ��ļ�����
	
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
