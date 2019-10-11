package com.system.fileoperations;

import com.system.entity.FileEntry;
import com.system.entity.Folder;
import com.system.model.FAT;
import com.system.openedfiletable.*;

public class creatFile {
	private String name; // �ļ���
	private boolean attribute; // �ļ�����
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
			System.out.println("���ļ�Ϊֻ���ļ������ܽ�����");
		}else {
			String[] eachname = name.split("/");
			for(int i = 0; i < eachname.length; i++) {
				Folder catalog = new Folder(2,eachname[i]);
				if(catalog.contain(eachname[i]) == false) {
					System.out.println("���ļ���Ŀ¼Ϊ�գ������ļ�ʧ�ܣ�");
					break;
				}else {
					flag = 1;
				}
			}
			if(flag == 1) {
				FileEntry filecatalog = new FileEntry(); //�����ļ�Ŀ¼
				FAT.getInstance();
				index = FAT.getInstance().allocation(); //����һ�����̿�
				//��дĿ¼
				OFTLE tablechoose = new OFTLE(this.name,this.attribute,this.index); 
				tablelength = openfiletable.savefile(tablechoose);//��д�Ѵ��ļ���
				if(tablelength == 6) {
					System.out.println("�Ѵ��ļ�����������");
				}
			}
		}
	}
}
