package com.system.model;

import com.system.entity.Entry;
import com.system.entity.FileEntry;
import com.system.entity.Folder;
import com.system.entity.FolderEntry;
import com.system.utils.FolderUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
     * 删除一个文件夹
     * 比如，删除/abc/def/cf
     * 传入path /abc/def
     * 传入name cf
     * @param path 父目录路径
     * @param name 名字
     *
     */
    public boolean deleteFolder(String path, String name){
        if(name.length() > 3){
            return false;
        }
        path = path.substring(1, path.length());
        LinkedList<String> list = null;
        if(path.equals("")){
            list = new LinkedList<>();
        }else{
            list = new LinkedList<>(Arrays.asList(path.split("/")));
        }
        Folder folder = new Folder(Disk.ROOT_FOLDER_INDEX, "/");
        return deleteFolder(list, folder, name);
    }

    private boolean deleteFolder(LinkedList<String> parentList, Folder parent, String name) {
        if(!parentList.isEmpty()){
            String nextParentName = parentList.removeFirst();
            if(!parent.contain(nextParentName)){
                return false;
            }
            Entry entry = parent.get(nextParentName);
            if(entry instanceof FileEntry){
                return false;
            }
            Folder folder = new Folder((FolderEntry) entry);
            return deleteFolder(parentList, folder, name);
        }else{
            if(!parent.contain(name)){
                return false;
            }
            if(parent.Empty()){
                return false;
            }
            return parent.deleteFolder(name);
        }
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

    /**
     * 获取文件夹下的所有文件或者目录
     * @param path 文件夹的父目录
     * @param name 文件夹的名字
     * /abc/def/c d 找c下面的d文件夹
     */
    private List<Entry> listFolderContentEntry(String path, String name){
        Folder folder = FolderUtils.findDeepestFolder(path);
        if(folder == null){
            return null;
        }
        Entry tempEntry = folder.get(name);
        if(tempEntry instanceof  FileEntry){
            return null;
        }
        folder = new Folder((FolderEntry) tempEntry);
        List<Entry> entryList = new ArrayList<>();
        for(Entry entry : folder.getEntries()){
            if(entry != null){
                entryList.add(entry);
            }
        }
        return entryList;
    }
}
