package com.system.model;

import com.system.entity.Entry;
import com.system.entity.FileEntry;
import com.system.entity.Folder;
import com.system.entity.FolderEntry;
import com.system.utils.FolderUtils;

import java.util.Arrays;
import java.util.LinkedList;

public class FileManager {

    /**
     * 创建一个文件
     * @param path
     * @param name
     * @return
     */
    public static FileEntry createFile(String path, String name){
        String[] nameArray = name.split("\\.");
        if(nameArray.length != 2 || nameArray[0].length() > 3){
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
        return createFile(list, folder, name);
    }

    private static FileEntry createFile(LinkedList<String> parentList, Folder parent, String name) {
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
            return createFile(parentList, folder, name);
        }else{
            if(!parent.contain(name)){
                return null;
            }
            if(parent.Empty()){
                return null;
            }
            return parent.createFile(name);
        }
    }

    /**
     * 删除一个文件
     * @param path 文件父路径
     * @param name 文件名
     * @return 是否成功删除这个文件
     */
    private static boolean deleteFile(String path, String name){
        Folder folder = FolderUtils.findDeepestFolder(path);
        if(folder == null){
            return false;
        }
        return folder.deleteFile(name);
    }


}
