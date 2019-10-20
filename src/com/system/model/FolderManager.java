package com.system.model;

import com.system.entity.Entry;
import com.system.entity.FileEntry;
import com.system.entity.Folder;
import com.system.entity.FolderEntry;
import java.util.Arrays;
import java.util.LinkedList;

public class FolderManager {
    /**
     * 创建一个文件夹
     * @param path 文件夹父目录路径
     * @param name 要创建的文件夹名称
     * @return 成功创建则返回一个entry，否则返回null
     */
    public FolderEntry createFolder(String path, String name){
        if(name.length() > 3){
            return null;
        }
        path = path.substring(1, path.length());
        LinkedList<String> list = null;
        if(path.equals("")){
            list = new LinkedList<>();
        }else{
            list = new LinkedList<>(Arrays.asList(path.split("/")));
        }
        Folder folder = new Folder(Disk.ROOT_FOLDER_INDEX, "/");
        return createFolder(list, folder, name);
    }

    /**
     * @param parentList 分割后的父目录名称
     * @param parent 父亲文件夹
     * @param name 要创建的文件夹名称
     * @return 成功创建则返回一个entry，否则返回null
     */
    private FolderEntry createFolder(LinkedList<String> parentList, Folder parent, String name){
        // 如果parentList不为空
       if(!parentList.isEmpty()){
           String nextParentName = parentList.removeFirst();
           if(!parent.contain(nextParentName)){
               return null;
           }
           Entry entry = parent.get(nextParentName);
           if(entry instanceof FileEntry){
               return null;
           }
           Folder folder = new Folder((FolderEntry) entry);
           return createFolder(parentList, folder, name);
       }else{
           if(parent.contain(name)){
               return null;
           }
           if(parent.Full()){
               return null;
           }
           return parent.createFolder(name);
       }
    }
}
