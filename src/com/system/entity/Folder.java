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
     * ɾ��һ��entry
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
    	fileOpenTable.setNumber(index);
    	fileOpenTable.getRead().setBnum(0);
    	fileOpenTable.getRead().setDnum(index);
    	fileOpenTable.getWrite().setBnum(0);
    	fileOpenTable.getWrite().setDnum(index);
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.add(fileOpenTable)) {
    		System.out.println("�ļ������ɹ���");
    		return fileEntry;
    	}else {
    		return null;
    	}
    }
    
    /**
     * ���ļ�����
     * @return
     */
    public FileEntry openFile(String path, String name, String type) {
    	String file_entry_folder;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	file_entry_folder = fileCatalog.getFilefolder(path, name); // ��ȡ�ļ����ϼ��ļ���
    	if(contain(name) || Full()) {
    		return null;
    	}
    	openFileEntry = (FileEntry) get(name); // ��ȡ�ļ����ϼ��ļ��е�Ŀ¼���и��ļ���Ŀ¼�ļ�
    	if(type.length() > 2) {
    		return null;
    	}
    	if(type.equals("ow") && !openFileEntry.isReadOnly()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) == null) { // �Ѵ��ļ�����û�м�¼������д�Ѵ��ļ���
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
     * ���ļ�
     * @return
     */
    public OpenFileTable readFile(String path, String name, int length) {
    	String filename = path + '/' + name;
    	OpenFileTable fileTable;
    	OpenFile fileOpen = new OpenFile();
    	int beginlength;
    	if(fileOpen.get(name) == null) { // �ж��Ѵ��ļ������Ƿ��и��ļ�
    		openFile(path, name, "or");
    	}
    	fileTable = fileOpen.get(name); // ��ø��ļ����Ѵ��ļ��ǼǱ�
    	if(fileTable.getFlag() == 1) {
    		return null;
    	}
    	beginlength = fileTable.getRead().getBnum(); // ��ȡ���ļ���ָ��Ŀ�ʼ
    	File file = new File(filename); // ���ַ������������
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
     * д�ļ�
     * @return
     */
    public OpenFileTable writeFile(String path, String name, int length) {
    	String filename = path + '/' + name;
    	OpenFileTable fileTable;
    	OpenFile fileOpen = new OpenFile();
    	int beginlength;
    	if(fileOpen.get(name) == null) { // �Ѵ��ļ�����û�и��ļ��������ȴ��ļ�
    		openFile(path, name, "ow");
    	}
    	fileTable = fileOpen.get(name); // ��ȡ���ļ����Ѵ��ļ��ǼǱ�
    	if(fileTable.getFlag() == 0) {
    		return null;
    	}
    	beginlength = fileTable.getWrite().getBnum();
    	File file = new File(filename);    //1����������
        OutputStream os = null;
        byte[] data = null;
        try {
            os = new FileOutputStream(file,true);
            Scanner input = new Scanner(System.in);
            String string = input.nextLine();
            data = string.getBytes();    //���ַ���ת��Ϊ�ֽ�����,��������д��

            os.write(data, 0, data.length);    //3��д���ļ�
            os.flush();    //���洢�ڹܵ��е�����ǿ��ˢ�³�ȥ
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("�ļ�û���ҵ���");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("д���ļ�ʧ�ܣ�");
        }finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("�ر������ʧ�ܣ�");
                }
            }
        }
        Disk disk = Disk.getInstance();
        disk.writeBlock(fileTable.getNumber(), length, data); // д�����
        fileTable.getWrite().setBnum(beginlength + length); // ��дָ���������
        return fileTable;
    }
    
    /**
     * �ر��ļ�
     * @return
     */
    public OpenFileTable closeFile(String path,String name) {
    	String filename = path + '/' + name;
    	OpenFileTable fileTable;
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) == null) { // �ж��ļ��Ƿ����Ѵ��ļ�����
    		return null;
    	}
    	fileTable = fileOpen.get(name);  // ��ȡ���ļ���Ŀ¼�ļ�
    	if(fileTable.getFlag() == 1) { // ���򿪷�ʽ �� 1 Ϊд���� ׷���ļ�������"#"
    		try {
    		FileOutputStream fos = new FileOutputStream(new File(filename));
    		String str = "#";
    		fos.write(str.getBytes());
    		fos.close();
    		}catch(IOException e){
    			e.printStackTrace();
    		}
    	}
    	if(!fileOpen.remove(name)) { // ���Ѵ��ļ�����ɾ���ļ�
    		return null;
    	}
    	return fileTable;
    }
    
    /**
     * ɾ���ļ�
     * @return
     */
    public FileEntry deleteFile(String path, String name) {
    	String file_entry;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	file_entry = fileCatalog.getFilefolder(path, name); // ��ȡ�ļ����ϼ��ļ���
    	if(contain(name) || Full()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) != null) { // �ж��Ѵ��ļ������Ƿ��м�¼���ļ�
    		return null;
    	}else {
    		openFileEntry = (FileEntry) get(name); // ��ȡ���ļ���Ŀ¼�ļ�
    		removeEntry(openFileEntry); // ɾ�����ļ���Ŀ¼�ļ�
    		FAT.getInstance().collect(openFileEntry.getStartBlockIndex()); // ���մ��̿�
    		return openFileEntry;
    	}
    }
    
    /**
     * ��ʾ�ļ�����
     * @return
     */
    public FileEntry typeFile(String path, String name) {
    	String file_entry;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	int startindex;
    	byte[] list = null; // ������
    	int count = 0;
    	file_entry = fileCatalog.getFilefolder(path, name); // ��ȡ�ļ����ϼ��ļ���
    	if(contain(name) || Full()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) != null) { // �ж��Ѵ��ļ������Ƿ��м�¼���ļ�
    		return null;
    	}
    	openFileEntry = (FileEntry)get(name);
    	startindex = openFileEntry.getStartBlockIndex();
    	while(count < openFileEntry.getLength()) {
    		list = Disk.getInstance().readBlock(startindex); // ��Disk�л�ö�Ӧ��ʼ���̺ŵ�һ��������
    		for(int i = 0; i < list.length; i++) {
    			System.out.print(list[i]);
    		}
    		count++ ;
    	}
    	return openFileEntry;
    }
    
    /**
     * �ı�����
     * @return
     */
    public FileEntry change(String path, String name, String type ) {
    	String file_entry;
    	FileEntry openFileEntry;
    	FileManager fileCatalog = new FileManager();
    	file_entry = fileCatalog.getFilefolder(path, name); // ��ȡ�ļ����ϼ��ļ���
    	if(contain(name) || Full()) {
    		return null;
    	}
    	OpenFile fileOpen = new OpenFile();
    	if(fileOpen.get(name) != null) { // �ж��Ѵ��ļ������Ƿ��м�¼���ļ�
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
