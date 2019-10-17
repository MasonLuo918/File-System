package com.system.model;

import java.util.Arrays;
import java.util.LinkedList;

import com.system.entity.Entry;
import com.system.entity.FileEntry;
import com.system.entity.Folder;
import com.system.entity.FolderEntry;

public class FileManager {
	
	public String getFilefolder(String path, String name) {
		String fileFolder;
		int length = 0;
		if (name.length() > 3) {
			return null;
		}
		path = path.substring(1, path.length());
		LinkedList<String> list = null;
		if (path.equals("")) {
			list = new LinkedList<>();
		} else {
			list = new LinkedList<>(Arrays.asList(path.split("/")));
			list.removeLast();
			fileFolder = list.getLast();
		}
		return fileFolder;
	}

	public FileEntry createFile(String path, String name,) {
    	String fileFolder;
    	if(name.length() > 3) {
    		return null;
    	}
    	path = path.substring(1,path.length()); // 去掉第一个字符'/'
    	LinkedList<String> list = null;
    	if(path.equals("")) {
    		list = new LinkedList<>();
    	}else {
    		list = new LinkedList<>(Arrays.asList(path.split("/")));
    	}
    	fileFolder = getFilefolder(path, name);
    	Folder folder = new Folder(Disk.ROOT_FOLDER_INDEX,"/");
    	return createFile(list, folder, name, fileFolder);
    }

	private FileEntry createFile(LinkedList<String> parentList, Folder parent, String name, String fileFolder) {
		if (!parentList.isEmpty()) {
			String nextParentName = parentList.removeFirst();
			if (!parent.contain(nextParentName)) {
				return null;
			}
			Entry entry = parent.get(nextParentName);
			if (entry instanceof FileEntry) {
				return null;
			}
			Folder folder = new Folder((FolderEntry) entry);
			return createFile(parentList, folder, name, fileFolder);
		} else {
			if (parent.contain(name)) {
				return null;
			}
			if (parent.Full()) {
				return null;
			}
			return parent.createFile(name, fileFolder);
		}
	}
}
