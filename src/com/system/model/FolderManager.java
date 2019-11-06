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
     * ɾ��һ���ļ���
     * ���磬ɾ��/abc/def/cf
     * ����path /abc/def
     * ����name cf
     * @param path ��Ŀ¼·��
     * @param name ����
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

    /**
     * ��ȡ�ļ����µ������ļ�����Ŀ¼
     * @param path �ļ��еĸ�Ŀ¼
     * @param name �ļ��е�����
     * /abc/def/c d ��c�����d�ļ���
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
