package com.system.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/*
 * 已打开文件登记表定义
 */
public class OpenFile extends Observable{
	private static final int n = 5;

	private static List<OpenFileTable> data = new ArrayList<>();
	
	private static OpenFile instance;
	
	private OpenFile() {};
	
	/**
	 * 取得唯一实例
	 * @return
	 */
	public static OpenFile getInstance() {
		if(instance == null) {
			instance = new OpenFile();
			instance.data = new ArrayList<OpenFileTable>();
		}
		return instance;
	}
	
	/**
	 * 增加观察者
	 * @return
	 */
	public void addOpenFile(Observer observer) {
		this.addObserver(observer);
	}

	/**
	 * 新增表项
	 * @param table
	 * @return
	 */
	public boolean add(OpenFileTable table) {
		if (data.size() >= n) {
			return false;
		}
		data.add(table);
		this.setChanged();
		this.notifyObservers(table);
		return true;
	}

	/**
	 * 删除表项
	 * @param fileName
	 * @return
	 */
	public boolean remove(String fileName) {
		if (data.size() == 0) {
			return false;
		}
		Iterator<OpenFileTable> iterator = data.iterator();
		while (iterator.hasNext()) {
			OpenFileTable table = iterator.next();
			if (table.getName().equals(fileName)) {
				iterator.remove();
				this.setChanged();
				this.notifyObservers();
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

	public static List<OpenFileTable> getData() {
		return data;
	}
}