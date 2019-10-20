package com.system.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
import com.sun.xml.internal.bind.v2.util.FatalAdapter;
import com.system.entity.OpenFileTable.Pointer;
import com.system.model.Disk;
import com.system.model.FAT;
import com.system.model.FileManager;
import com.system.utils.PropertyUtils;

import javafx.scene.chart.PieChart.Data;

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
     * 删除一个entry
     */
    private boolean removeEntry(Entry entry) {
    	if(entries.length == 0) {
    		return false; 
    	}
    	List<Entry> list = Arrays.asList(entries);
    	List<Entry> entryList = new ArrayList<Entry>(list); 
		entryList.remove(entry);
		return true;
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
    	fileOpenTable.setNumber(index);
    	fileOpenTable.getRead().setBnum(0);
    	fileOpenTable.getRead().setDnum(index);
    	fileOpenTable.getWrite().setBnum(0);
    	fileOpenTable.getWrite().setDnum(index);
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.add(fileOpenTable)) {
    		System.out.println("文件创建成功！");
    		return fileEntry;
    	}else {
    		return null;
    	}
    }
    
    /**
     * 打开文件操作
     * @return
     */
    public FileEntry openFile(String path, String name, String type) {
    	String file_entry_folder;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	file_entry_folder = fileCatalog.getFilefolder(path, name); // 获取文件的上级文件夹
    	if(contain(name) || Full()) {
    		return null;
    	}
    	openFileEntry = (FileEntry) get(name); // 获取文件的上级文件夹的目录项中该文件的目录文件
    	if(type.length() > 2) {
    		return null;
    	}
    	if(type.equals("ow") && !openFileEntry.isReadOnly()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) == null) { // 已打开文件表中没有记录，则填写已打开文件表
    		byte attribute = openFileEntry.getPropertyForByte();
    		OpenFileTable fileOpenTable = new OpenFileTable();
    		fileOpenTable.setName(name);
    		fileOpenTable.setAttribute(attribute);
    		fileOpenTable.setNumber(openFileEntry.getStartBlockIndex());
    		fileOpenTable.getRead().setBnum(0);
    		fileOpenTable.getRead().setDnum(openFileEntry.getStartBlockIndex());
    		fileOpenTable.getWrite().setBnum(0);
    		fileOpenTable.getWrite().setDnum(openFileEntry.getStartBlockIndex());
    		if(type.equals("ow")) {
    			fileOpenTable.setFlag(1);
    		}else if(type.equals("or")) {
    			fileOpenTable.setFlag(0);
    		}
    		fileOpen.add(fileOpenTable);
    	}
    	return openFileEntry;
    }
    
    /**
     * 读文件
     * @return
     */
    public OpenFileTable readFile(String path, String name, int length) {
    	String filename = path + '/' + name;
    	OpenFileTable fileTable;
    	OpenFile fileOpen = new OpenFile();
    	int beginlength;
    	if(fileOpen.get(name) == null) { // 判断已打开文件表中是否含有该文件
    		openFile(path, name, "or");
    	}
    	fileTable = fileOpen.get(name); // 获得该文件的已打开文件登记表
    	if(fileTable.getFlag() == 1) {
    		return null;
    	}
    	beginlength = fileTable.getRead().getBnum(); // 获取该文件读指针的开始
    	File file = new File(filename); // 以字符数组读入数据
    	Reader reader = null;
    	try {
    		char[] tempchars = new char[length - beginlength];
    		int charread = 0;
    		reader = new InputStreamReader(new FileInputStream(name));
    		while((charread = reader.read(tempchars)) != -1) {
    			if((charread == tempchars.length) && (tempchars[tempchars.length-1] != '\r' || tempchars[charread] == '#')) {
    			System.out.print(tempchars);
    			}else {
    				for(int i = 0; i < charread; i++) {
    					if(tempchars[i] == '\r') {
    						continue;
    					}else if(tempchars[i] == '#') {
    						break;
    					}else {
    						System.out.print(tempchars[i]);
    					}
    				}
    			}
    		}
    	}catch(Exception e1) {
    		e1.printStackTrace();
    	}finally {
    		if(reader != null) {
    			try {
    				reader.close();
    			}catch (IOException e1) {
				}
    		}
    	}
    	return fileTable;
    }
    
    /**
     * 写文件
     * @return
     */
    public OpenFileTable writeFile(String path, String name, int length) {
    	String filename = path + '/' + name;
    	OpenFileTable fileTable;
    	OpenFile fileOpen = new OpenFile();
    	int beginlength;
    	if(fileOpen.get(name) == null) { // 已打开文件表中没有该文件，则需先打开文件
    		openFile(path, name, "ow");
    	}
    	fileTable = fileOpen.get(name); // 获取该文件的已打开文件登记表
    	if(fileTable.getFlag() == 0) {
    		return null;
    	}
    	beginlength = fileTable.getWrite().getBnum();
    	File file = new File(filename);    //1、建立连接
        OutputStream os = null;
        byte[] data = null;
        try {
            os = new FileOutputStream(file,true);
            Scanner input = new Scanner(System.in);
            String string = input.nextLine();
            data = string.getBytes();    //将字符串转换为字节数组,方便下面写入

            os.write(data, 0, data.length);    //3、写入文件
            os.flush();    //将存储在管道中的数据强制刷新出去
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("文件没有找到！");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("写入文件失败！");
        }finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("关闭输出流失败！");
                }
            }
        }
        Disk disk = Disk.getInstance();
        disk.writeBlock(fileTable.getNumber(), length, data); // 写入磁盘
        fileTable.getWrite().setBnum(beginlength + length); // 将写指针移至最后
        return fileTable;
    }
    
    /**
     * 关闭文件
     * @return
     */
    public OpenFileTable closeFile(String path,String name) {
    	String filename = path + '/' + name;
    	OpenFileTable fileTable;
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) == null) { // 判断文件是否在已打开文件表中
    		return null;
    	}
    	fileTable = fileOpen.get(name);  // 获取该文件的目录文件
    	if(fileTable.getFlag() == 1) { // 检查打开方式 ， 1 为写操作 追加文件结束符"#"
    		try {
    		FileOutputStream fos = new FileOutputStream(new File(filename));
    		String str = "#";
    		fos.write(str.getBytes());
    		fos.close();
    		}catch(IOException e){
    			e.printStackTrace();
    		}
    	}
    	if(!fileOpen.remove(name)) { // 从已打开文件表中删除文件
    		return null;
    	}
    	return fileTable;
    }
    
    /**
     * 删除文件
     * @return
     */
    public FileEntry deleteFile(String path, String name) {
    	String file_entry;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	file_entry = fileCatalog.getFilefolder(path, name); // 获取文件的上级文件夹
    	if(contain(name) || Full()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) != null) { // 判断已打开文件表里是否有记录该文件
    		return null;
    	}else {
    		openFileEntry = (FileEntry) get(name); // 获取该文件的目录文件
    		removeEntry(openFileEntry); // 删除该文件的目录文件
    		FAT.getInstance().collect(openFileEntry.getStartBlockIndex()); // 回收磁盘块
    		return openFileEntry;
    	}
    }
    
    /**
     * 显示文件内容
     * @return
     */
    public FileEntry typeFile(String path, String name) {
    	String file_entry;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	int startindex;
    	byte[] list = null; // 缓冲区
    	int count = 0;
    	file_entry = fileCatalog.getFilefolder(path, name); // 获取文件的上级文件夹
    	if(contain(name) || Full()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) != null) { // 判断已打开文件表里是否有记录该文件
    		return null;
    	}
    	openFileEntry = (FileEntry)get(name);
    	startindex = openFileEntry.getStartBlockIndex();
    	while(count < openFileEntry.getLength()) {
    		list = Disk.getInstance().readBlock(startindex); // 从Disk中获得对应起始磁盘号的一整块内容
    		for(int i = 0; i < list.length; i++) {
    			System.out.print(list[i]);
    		}
    		count++ ;
    	}
    	return openFileEntry;
    }
    
    /**
     * 改变属性
     * @return
     */
    public FileEntry change(String path, String name, String type ) {
    	String file_entry;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	file_entry = fileCatalog.getFilefolder(path, name); // 获取文件的上级文件夹
    	if(contain(name) || Full()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) != null) { // 判断已打开文件表里是否有记录该文件
    		return null;
    	}
    	openFileEntry = (FileEntry)get(name);
    	openFileEntry.setType(type);
    	return openFileEntry;
    }
    
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
