package com.system.model;

import com.system.entity.OpenFileTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author masonluo
 * @date 2019/11/5 11:23 PM
 */
public class OpenFileTableView {
    private static List<OpenFileTable> openFileTableList;
    static {
        openFileTableList = new ArrayList<>(5);
    }

    public static boolean add(OpenFileTable table){
        if(openFileTableList.size() <= 5 && get(table.getName()) == null){
            openFileTableList.add(table);
            return true;
        }
        return false;
    }

    public static boolean remove(String filename){
        Iterator<OpenFileTable> iterator = openFileTableList.iterator();
        while(iterator.hasNext()){
            OpenFileTable table = iterator.next();
            if(table.getName().equals(filename)){
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public static OpenFileTable get(String filename){
        for(OpenFileTable table : openFileTableList){
            if(table.getName().equals(filename)){
                return table;
            }
        }
        return null;
    }

    public static List<OpenFileTable> getOpenFileTableList() {
        return openFileTableList;
    }
}
