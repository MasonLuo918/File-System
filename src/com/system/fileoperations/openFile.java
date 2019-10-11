package com.system.fileoperations;

import com.system.entity.FileEntry;
import com.system.openedfiletable.OpenFile;
import com.system.entity.*;

public class openFile {
	private String name;
	private int type;
	
	public openFile(String name, int type) {
		this.name = name;
		this.type = type;
	}
	
	public void open_file(String name,int type) {
		FileEntry filecatalog = new FileEntry();
		int index = filecatalog.getStartBlockIndex();
		Folder isfile = new Folder(index, name);
		OpenFile openedtable = new OpenFile();
		if(isfile.contain(name) == false) {
			System.out.println("该文件不存在！");
		}
		if(filecatalog.isReadOnly()) {
			if()
		}
	}
}
