package com.system.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
 * 已打开文件登记表定义
 */
public class OpenFile {
	private static final int n = 5;

	private static List<OpenFileTable> data = new ArrayList<>();

	public boolean add(OpenFileTable table) {
		if (data.size() >= n) {
			return false;
		}
		data.add(table);
		return true;
	}

	public boolean remove(String fileName) {
		if (data.size() == 0) {
			return false;
		}
		Iterator<OpenFileTable> iterator = data.iterator();
		while (iterator.hasNext()) {
			OpenFileTable table = iterator.next();
			if (table.getName().equals(fileName)) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	public OpenFileTable get(String fileName) {
		for (OpenFileTable table : data) {
			if (table.getName().equals(fileName)) {
				return table;
			}
		}
		return null;
	}
}