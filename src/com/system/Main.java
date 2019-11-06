package com.system;

import com.system.entity.Folder;
import com.system.entity.OpenFileTable;
import com.system.exception.FileAlreadyOpenException;
import com.system.exception.OutOfLengthException;
import com.system.model.Disk;
import com.system.model.FAT;
import com.system.model.FolderManager;
import com.system.model.OpenFileTableView;

import java.util.List;

/**
 * @author masonluo
 * @date 2019/11/6 3:23 PM
 */
public class Main {
    public static void main(String[] args) throws OutOfLengthException, FileAlreadyOpenException {
        Disk disk = Disk.getInstance();
        FAT fat = FAT.getInstance();
        List<OpenFileTable> list = OpenFileTableView.getOpenFileTableList();
        FolderManager folderManager = new FolderManager();
        Folder folder = new Folder(Disk.ROOT_FOLDER_INDEX, "/");
        System.out.println(folderManager.createFolder("/", "ab"));
        System.out.println(folderManager.createFolder("/ab", "bc"));
        System.out.println(folderManager.deleteFolder("/", "ab"));
        System.out.println(folderManager.deleteFolder("/ab", "bc"));
    }
}
