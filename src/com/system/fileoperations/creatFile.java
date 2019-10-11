package com.system.fileoperations;

import com.system.entity.FileEntry;
import com.system.entity.Folder;
import com.system.model.FAT;
import com.system.openedfiletable.*;

public class creatFile {
	private String name; // 文件名
	private boolean attribute; // 文件属性
	private int index;
	
	public creatFile(String name) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.attribute = false;
	}
	
	public void creat_file(String name, boolean attribute) {
		OpenFile openfiletable = new OpenFile();
		int flag = 0;
		int tablelength;
		if(attribute == true) {
			System.out.println("该文件为只读文件，不能建立！");
		}else {
			String[] eachname = name.split("/");
			for(int i = 0; i < eachname.length; i++) {
				Folder catalog = new Folder(2,eachname[i]);
				if(catalog.contain(eachname[i]) == false) {
					System.out.println("该文件父目录为空，建立文件失败！");
					break;
				}else {
					flag = 1;
				}
			}
			if(flag == 1) {
				FileEntry filecatalog = new FileEntry(); //建立文件目录
				FAT.getInstance();
				index = FAT.getInstance().allocation(); //分配一个磁盘块
				//填写目录
				OFTLE tablechoose = new OFTLE(this.name,this.attribute,this.index); 
				tablelength = openfiletable.savefile(tablechoose);//填写已打开文件表
				if(tablelength == 6) {
					System.out.println("已打开文件数量已满！");
				}
			}
		}
	}
}
