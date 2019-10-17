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
 * �ļ��У���һ���̿��ȡ��һ���ļ��У��������
 */
public class Folder {

    // �ļ�������
    private String name;

    // ��ȡ���̿�����
    private int blockIndex;

    // ���̿��Ŀ¼��(��Ŀ¼û��)
    private Entry myself;

    // ���̿������Ŀ¼��
    private Entry[] entries = new Entry[8];

    /**
     * @param blockIndex ��ȡ���̿�
     * @param name ��Ŀ¼������
     */
    public Folder(int blockIndex, String name) {
        this.name = name;
        this.blockIndex = blockIndex;
        loadEntry(blockIndex);
        // Ĭ�����óɸ�Ŀ¼entry
        myself = new FolderEntry();
        myself.setName(name);
        myself.setNormal(true);
        myself.setFolder(true);
    }

    /**
     * @param folderEntry ���ļ��е�Ŀ¼��
     */
    public Folder(FolderEntry folderEntry){
        this(folderEntry.getStartBlockIndex(), folderEntry.getName());
        myself = folderEntry;
    }
    /**
     * ����һ���ļ���
     * @param name �ļ���
     * @return �����ɹ������ļ���Entry�����򷵻�NULL
     */
    public FolderEntry createFolder(String name){
        return createFolder(name, false);
    }
    /**
     * ����һ���ļ���
     * @param name �ļ���
     * @param readOnly �ļ������ԣ��Ƿ�ֻ��
     * @return �����ɹ������ļ���Entry�����򷵻�NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly){
        return createFolder(name, readOnly, false);
    }
    /**
     * ����һ���ļ���
     * @param name �ļ���
     * @param readOnly �ļ������ԣ��Ƿ�ֻ��
     * @param system �ļ������ԣ��Ƿ�ϵͳ�ļ�
     * @return �����ɹ������ļ���Entry�����򷵻�NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly, boolean system){
        return createFolder(name, readOnly, system, true);
    }
    /**
     * ����һ���ļ���
     * @param name �ļ���
     * @param readOnly �ļ������ԣ��Ƿ�ֻ��
     * @param system �ļ������ԣ��Ƿ�ϵͳ�ļ�
     * @param normal �ļ������ԣ��Ƿ���ͨ�ļ�
     * @return �����ɹ������ļ���Entry�����򷵻�NULL
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
     * ���һ��Entry������д�������
     * @param entry Ҫ��ӵ�entry
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
     * ��ʼ��һ���ļ��У�����Ŀ¼��ȫ���ÿ�
     * @param folderEntry �ļ���Entry
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
     * ��ȡһ��ָ�����ֵ�Entry
     * @param name entry����
     * @return ��Ӧ��entry������������򷵻�null
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
     * �ļ����Ƿ�����
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
     * �ļ����Ƿ�Ϊ��
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
     * �Ƿ��и��ļ�
     * @param name �ļ�entry��
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
     * ����һ���ļ���
     * @param index �̿�index
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
     * ����һ���ļ�
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
    	FileEntry fileEntry = new FileEntry(); // ��д�ļ�Ŀ¼
    	fileEntry.setFolder(true);
    	fileEntry.setName(name);
    	fileEntry.setReadOnly(readOnly);
    	fileEntry.setNormal(normal);
    	fileEntry.setStartBlockIndex(index);
    	Folder folder = new Folder(index, name);
    	folder.addEntry(fileEntry); // �����ļ�Ŀ¼
    	OpenFileTable fileOpenTable = new OpenFileTable(); // ��д�Ѵ��ļ���
    	fileOpenTable.setAttribute(fileEntry.getPropertyForByte());
    	fileOpenTable.setName(name);
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.add(fileOpenTable)) {
    		System.out.println("�ļ������ɹ���");
    		return fileEntry;
    	}
    }
    
    /**
     * ���ļ�����
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
     * ���ļ�
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
