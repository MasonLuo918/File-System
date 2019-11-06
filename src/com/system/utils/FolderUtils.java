package com.system.utils;

import com.system.entity.Entry;
import com.system.entity.FileEntry;
import com.system.entity.Folder;
import com.system.entity.FolderEntry;
import com.system.model.Disk;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author masonluo
 * @date 2019/11/6 9:58 PM
 */
public class FolderUtils {
    public static Folder findDeepestFolder(String path){
        path = path.substring(1, path.length());
        LinkedList<String> list = null;
        if(path.equals("")){
            list = new LinkedList<>();
        }else{
            list = new LinkedList<>(Arrays.asList(path.split("/")));
        }
        Folder folder = new Folder(Disk.ROOT_FOLDER_INDEX, "/");
        return findDeepestFolder(list, folder);
    }

    public static Folder findDeepestFolder(LinkedList<String> parentList, Folder parent) {
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
            return findDeepestFolder(parentList, folder);
        }else{
            return parent;
        }
    }
}
