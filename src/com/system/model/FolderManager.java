package com.system.model;

import com.system.entity.Entry;
import com.system.entity.FileEntry;
import com.system.entity.Folder;
import com.system.entity.FolderEntry;
import java.util.Arrays;
import java.util.LinkedList;

public class FolderManager {
    /**
     * ����һ���ļ���
     * @param path �ļ��и�Ŀ¼·��
     * @param name Ҫ�������ļ�������
     * @return �ɹ������򷵻�һ��entry�����򷵻�null
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
     * @param parentList �ָ��ĸ�Ŀ¼����
     * @param parent �����ļ���
     * @param name Ҫ�������ļ�������
     * @return �ɹ������򷵻�һ��entry�����򷵻�null
     */
    private FolderEntry createFolder(LinkedList<String> parentList, Folder parent, String name){
        // ���parentList��Ϊ��
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
