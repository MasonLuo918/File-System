package com.system.entity;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
import com.sun.xml.internal.ws.handler.HandlerProcessor.RequestOrResponse;
import com.system.model.Disk;
import com.system.model.FAT;
import com.system.model.FileManager;
import com.system.utils.PropertyUtils;

/**
 * 文件夹，将一个盘块读取成一个文件夹，方便操作
 */
public class Folder {

    // 文件夹名字
    private String name;

    // 读取的盘块索引
    private int blockIndex;

    // 该盘块的目录项(根目录没有)
    private Entry myself;

    // 该盘块的所有目录项
    private Entry[] entries = new Entry[8];

    /**
     * @param blockIndex 读取的盘块
     * @param name 该目录的名字
     */
    public Folder(int blockIndex, String name) {
        this.name = name;
        this.blockIndex = blockIndex;
        loadEntry(blockIndex);
        // 默认设置成根目录entry
        myself = new FolderEntry();
        myself.setName(name);
        myself.setNormal(true);
        myself.setFolder(true);
    }

    /**
     * @param folderEntry 该文件夹的目录项
     */
    public Folder(FolderEntry folderEntry){
        this(folderEntry.getStartBlockIndex(), folderEntry.getName());
        myself = folderEntry;
    }
    /**
     * 创建一个文件夹
     * @param name 文件名
     * @return 创建成功返回文件加Entry，否则返回NULL
     */
    public FolderEntry createFolder(String name){
        return createFolder(name, false);
    }
    /**
     * 创建一个文件夹
     * @param name 文件名
     * @param readOnly 文件夹属性，是否只读
     * @return 创建成功返回文件加Entry，否则返回NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly){
        return createFolder(name, readOnly, false);
    }
    /**
     * 创建一个文件夹
     * @param name 文件名
     * @param readOnly 文件夹属性，是否只读
     * @param system 文件夹属性，是否系统文件
     * @return 创建成功返回文件加Entry，否则返回NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly, boolean system){
        return createFolder(name, readOnly, system, true);
    }
    /**
     * 创建一个文件夹
     * @param name 文件名
     * @param readOnly 文件夹属性，是否只读
     * @param system 文件夹属性，是否系统文件
     * @param normal 文件夹属性，是否普通文件
     * @return 创建成功返回文件加Entry，否则返回NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly, boolean system, boolean normal){
        if(contain(name) || Full()){
            return null;
        }
        FAT fat = FAT.getInstance();
        int index = fat.allocation();
        if(index == -1){
            return null;
        }
        FolderEntry folderEntry = new FolderEntry();
        folderEntry.setFolder(true);
        folderEntry.setReadOnly(readOnly);
        folderEntry.setSystem(system);
        folderEntry.setNormal(normal);
        folderEntry.setName(name);
        folderEntry.setStartBlockIndex(index);
        addEntry(folderEntry);
        initFolder(folderEntry);
        return folderEntry;
    }

    /**
     * 添加一个Entry，并且写入磁盘中
     * @param entry 要添加的entry
     */
    private void addEntry(Entry entry){
        int i;
        for(i = 0; i < entries.length; i++){
            if(entries[i] == null){
                entries[i] = entry;
                break;
            }
        }
        Disk.getInstance().writeBlock(blockIndex, i * 8, i * 8 + 8, entry.toBytes());
    }
    /**
     * 初始化一个文件夹，将其目录项全部置空
     * @param folderEntry 文件夹Entry
     */
    private void initFolder(FolderEntry folderEntry){
        int index = folderEntry.getStartBlockIndex();
        byte[] blockData = new byte[64];
        for(int i = 0; i < blockData.length; i++){
            blockData[i] = 0;
        }
        for(int i = 0; i < 8; i++){
            blockData[i * 8] = '$';
        }
        Disk.getInstance().writeBlock(index, blockData);
    }
    /**
     * 获取一个指定名字的Entry
     * @param name entry名字
     * @return 对应的entry，如果不存在则返回null
     */
    public Entry get(String name){
        for(Entry entry : entries){
            if(entry.getName().equals(name)){
                return entry;
            }
        }
        return null;
    }
    /**
     * 文件夹是否满了
     */
    public boolean Full(){
        for(Entry entry : entries){
            if(entry == null){
                return false;
            }
        }
        return true;
    }
    /**
     * 文件夹是否为空
     */
    public boolean Empty(){
        for(Entry entry : entries){
            if(entry != null){
                return false;
            }
        }
        return true;
    }

    /**
     * 是否含有该文件
     * @param name 文件entry名
     * @return boolean
     */
    public boolean contain(String name){
        for(Entry entry : entries){
            if(entry != null && entry.getName().equals(name)){
                return true;
            }
        }
        return false;
    }
    /**
     * 加载一个文件夹
     * @param index 盘块index
     */
    private void loadEntry(int index) {
        Disk disk = Disk.getInstance();
        byte[] data = disk.readBlock(index);
        for (int i = 0; i < 8; i++) {
            byte[] temp = new byte[8];
            System.arraycopy(data, i * 8, temp, 0, temp.length);
            if (temp[0] == '$') {
                entries[i] = null;
            } else {
                if(PropertyUtils.isFolder(temp[5])){
                    entries[i] = new FolderEntry(temp);
                }else{
                    entries[i] = new FileEntry(temp);
                }
            }
        }
    }
    
    /**
     * 创建一个文件
     * @return
     */
    
    public FileEntry createFile(String name, String fileFolder) {
    	return createFile(name, fileFolder, false);
    }
    public FileEntry createFile(String name, String fileFolder, boolean readOnly) {
		return createFile(name, fileFolder, readOnly,true);
	}
    public FileEntry createFile(String name, String fileFolder, boolean readOnly, boolean normal) {
    	if(contain(name) || Full()) {
    		return null;
    	}
    	FAT fat = FAT.getInstance();
    	int index = fat.allocation();
    	if(index == -1) {
    		return null;
    	}
    	FileEntry fileEntry = new FileEntry(); // 填写文件目录
    	fileEntry.setFolder(true);
    	fileEntry.setName(name);
    	fileEntry.setReadOnly(readOnly);
    	fileEntry.setNormal(normal);
    	fileEntry.setStartBlockIndex(index);
    	Folder folder = new Folder(index, name);
    	folder.addEntry(fileEntry); // 保存文件目录
    	OpenFileTable fileOpenTable = new OpenFileTable(); // 填写已打开文件表
    	fileOpenTable.setAttribute(fileEntry.getPropertyForByte());
    	fileOpenTable.setName(name);
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.add(fileOpenTable)) {
    		System.out.println("文件创建成功！");
    		return fileEntry;
    	}
    }
    
    /**
     * 打开文件操作
     * @return
     */
    public FileEntry openFile(String path, String name, String type) {
    	String file_entry;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	file_entry = fileCatalog.getFilefolder(path, name);
    	if(contain(name) || Full()) {
    		return null;
    	}
    	openFileEntry = (FileEntry) get(name);
    	if(type.length() > 2) {
    		return null;
    	}
    	if(type.equals("ow") && !openFileEntry.isReadOnly()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) == null) {
    		byte attribute = openFileEntry.getPropertyForByte();
    		OpenFileTable fileOpenTable = new OpenFileTable();
    		fileOpenTable.setName(name);
    		fileOpenTable.setAttribute(attribute);
    		fileOpen.add(fileOpenTable);
    	}
    	return openFileEntry;
    }
    
    /**
     * 读文件
     * @return
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public void setBlockIndex(int blockIndex) {
        this.blockIndex = blockIndex;
    }

    public Entry getMyself() {
        return myself;
    }

    public void setMyself(Entry myself) {
        this.myself = myself;
    }
}
