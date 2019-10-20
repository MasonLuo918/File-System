package com.system.common.observer;

import java.util.Observable;
import java.util.Observer;

import com.system.entity.OpenFileTable;

public class OpenFileObserver implements Observer{
	
	@Override
	public void update(Observable o, Object table) {
		OpenFileTable newtable = (OpenFileTable) table;
	}
}
