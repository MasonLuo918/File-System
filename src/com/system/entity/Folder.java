package com.system.entity;

import com.system.common.observer.OpenFileObserver;
import com.system.exception.FileAlreadyOpenException;
import com.system.exception.OutOfLengthException;
import com.system.model.Disk;
import com.system.model.FAT;
import com.system.model.FileManager;
import com.system.model.OpenFileTableView;
import com.system.utils.PropertyUtils;

import java.io.*;
import java.util.*;

/**
 * �ļ��У���һ���̿��ȡ��һ���ļ��У��������
 */
public class Folder {

    private static final char END_FILE_FLAG = 0;

    private static final byte[] EMPTY_ENTRY = new byte[]{
            '$','$','$','$','$','$','$','$'
    };

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
     * @param name       ��Ŀ¼������
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
    public Folder(FolderEntry folderEntry) {
        this(folderEntry.getStartBlockIndex(), folderEntry.getName());
        myself = folderEntry;
    }

    /**
     * ����һ���ļ���
     *
     * @param name �ļ���
     * @return �����ɹ������ļ���Entry�����򷵻�NULL
     */
    public FolderEntry createFolder(String name) {
        return createFolder(name, false);
    }

    /**
     * ����һ���ļ���
     *
     * @param name     �ļ���
     * @param readOnly �ļ������ԣ��Ƿ�ֻ��
     * @return �����ɹ������ļ���Entry�����򷵻�NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly) {
        return createFolder(name, readOnly, false);
    }

    /**
     * ����һ���ļ���
     *
     * @param name     �ļ���
     * @param readOnly �ļ������ԣ��Ƿ�ֻ��
     * @param system   �ļ������ԣ��Ƿ�ϵͳ�ļ�
     * @return �����ɹ������ļ���Entry�����򷵻�NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly, boolean system) {
        return createFolder(name, readOnly, system, true);
    }

    /**
     * ����һ���ļ���
     *
     * @param name     �ļ���
     * @param readOnly �ļ������ԣ��Ƿ�ֻ��
     * @param system   �ļ������ԣ��Ƿ�ϵͳ�ļ�
     * @param normal   �ļ������ԣ��Ƿ���ͨ�ļ�
     * @return �����ɹ������ļ���Entry�����򷵻�NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly, boolean system, boolean normal) {
        if (contain(name) || Full()) {
            return null;
        }
        FAT fat = FAT.getInstance();
        int index = fat.allocation();
        if (index == -1) {
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

    public boolean deleteFolder(String name){
        Entry entry = get(name);
        if(!(entry instanceof FolderEntry)){
            return false;
        }
        FolderEntry folderEntry = (FolderEntry) entry;
        Folder folder = new Folder(folderEntry);
        if(!folder.Empty()){
            return false;
        }
        // ɾ��
        deleteEntry(entry);
        // ����
        int blockIndex = folder.getBlockIndex();
        FAT.getInstance().collect(blockIndex);
        return true;
    }

    public boolean deleteFile(String name){
        String[] nameArray = name.split("\\.");
        Entry entry = get(nameArray[0]);
        if(entry == null){
            return false;
        }
        int blockIndex = entry.getStartBlockIndex();
        FAT fat = FAT.getInstance();
        while(blockIndex != -1){
            int nextBlock = fat.getContent(blockIndex);
            fat.collect(blockIndex);
            blockIndex = nextBlock;
        }
        return deleteEntry(entry);
    }

    /**
     * ���һ��Entry������д�������
     *
     * @param entry Ҫ��ӵ�entry
     */
    private void addEntry(Entry entry) {
        int i;
        for (i = 0; i < entries.length; i++) {
            if (entries[i] == null) {
                entries[i] = entry;
                break;
            }
        }
        Disk.getInstance().writeBlock(blockIndex, i * 8, i * 8 + 8, entry.toBytes());
    }

    private boolean deleteEntry(Entry entry){
        if(entry == null){
            return false;
        }
        int i;
        for(i = 0; i < entries.length; i++){
            if(entries[i] == entry){
                break;
            }
        }
        if(i >= entries.length){
            return false;
        }
        Disk.getInstance().writeBlock(blockIndex, i * 8, (i + 1) * 8, EMPTY_ENTRY);
        entries[i] = null;
        return true;
    }

    /**
     * ɾ��һ��entry
     */
    private boolean removeEntry(Entry entry) {
        if (entries.length == 0) {
            return false;
        }
        List<Entry> list = Arrays.asList(entries);
        List<Entry> entryList = new ArrayList<Entry>(list);
        entryList.remove(entry);
        return true;
    }

    /**
     * ��ʼ��һ���ļ��У�����Ŀ¼��ȫ���ÿ�
     *
     * @param folderEntry �ļ���Entry
     */
    private void initFolder(FolderEntry folderEntry) {
        int index = folderEntry.getStartBlockIndex();
        byte[] blockData = new byte[64];
        for (int i = 0; i < blockData.length; i++) {
            blockData[i] = 0;
        }
        for (int i = 0; i < 8; i++) {
            blockData[i * 8] = '$';
        }
        Disk.getInstance().writeBlock(index, blockData);
    }

    /**
     * ��ȡһ��ָ�����ֵ�Entry
     *
     * @param name entry����
     * @return ��Ӧ��entry������������򷵻�null
     */
    public Entry get(String name) {
        for (Entry entry : entries) {
            if (entry != null && entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * �ļ����Ƿ�����
     */
    public boolean Full() {
        for (Entry entry : entries) {
            if (entry == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * �ļ����Ƿ�Ϊ��
     */
    public boolean Empty() {
        for (Entry entry : entries) {
            if (entry != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * �Ƿ��и��ļ�
     *
     * @param name �ļ�entry��
     * @return boolean
     */
    public boolean contain(String name) {
        for (Entry entry : entries) {
            if (entry != null && entry.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean closeFile(String name){
        return OpenFileTableView.remove(name);
    }

    /**
     * ����һ���ļ���
     *
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
                if (PropertyUtils.isFolder(temp[5])) {
                    entries[i] = new FolderEntry(temp);
                } else {
                    entries[i] = new FileEntry(temp);
                }
            }
        }
    }

    /**
     * ����һ���ļ�
     *
     * @return
     */
    public FileEntry createFile(String name) {
        return createFile(name, false);
    }

    public FileEntry createFile(String name, boolean readOnly) {
        return createFile(name, readOnly, true);
    }

    public FileEntry createFile(String name, boolean readOnly, boolean normal){
        return createFile(name, readOnly, normal, false);
    }

    /**
     * �½�һ���ļ�
     *
     * @param name     �ļ���
     * @param readOnly �Ƿ�ֻ��
     * @param normal   ��ͨ�ļ�
     * @return Entry
     */
    public FileEntry createFile(String name, boolean readOnly, boolean normal, boolean system) {
        String[] nameArray = name.split("\\.");
        if (nameArray.length != 2 || nameArray[0].length() > 3) {
            return null;
        }
        if (contain(nameArray[0]) || Full()) {
            return null;
        }
        FAT fat = FAT.getInstance();
        int index = fat.allocation();
        if (index == -1) {
            return null;
        }
        fullEndFileFlag(index);
        FileEntry fileEntry = new FileEntry();
        fileEntry.setName(nameArray[0]);
        fileEntry.setReadOnly(readOnly);
        fileEntry.setNormal(normal);
        fileEntry.setFolder(false);
        fileEntry.setSystem(false);
        fileEntry.setStartBlockIndex(index);
        fileEntry.setType(nameArray[1]);
        fileEntry.setLength(1);
        addEntry(fileEntry);
        return fileEntry;
    }

    /**
     * ���ļ�����
     * @param name �ļ�����
     * @param flag 0Ϊ����1Ϊд��Ĭ�϶�дһ��,flag=1��
     * @throws Exception ����ļ��Ѿ��򿪣����׳��쳣
     * @return
     * ������ܴ򿪣��򷵻�null(�Ѵ��ļ����������������ļ����ƴ���)
     * ����ɹ��򿪣����ض�Ӧ��entry
     */
    public FileEntry openFile(String name, int flag) throws FileAlreadyOpenException, OutOfLengthException {
        String[] nameArray = name.split("\\.");
        if(nameArray.length != 2){
            return null;
        }
        Entry entry = get(nameArray[0]);
        if(!(entry instanceof FileEntry)){
            return null;
        }
        FileEntry fileEntry = (FileEntry) entry;
        if(!fileEntry.getType().equals(nameArray[1])){
            return null;
        }
        if(OpenFileTableView.get(name) != null){
            throw new FileAlreadyOpenException();
        }
        if(OpenFileTableView.getOpenFileTableList().size() >= 5){
            throw new OutOfLengthException("�Ѵ��ļ�������������޷��������ļ�");
        }
        OpenFileTable openFileTable = new OpenFileTable(fileEntry, name, fileEntry.getPropertyForByte(), fileEntry.getStartBlockIndex(), fileEntry.getLength(), flag);
        boolean success = OpenFileTableView.add(openFileTable);
        if(success){
            return fileEntry;
        }
        return null;
    }

    /**
     * @param name �ļ���
     * @return �����ļ���string�ַ���
     * @throws FileAlreadyOpenException ����ļ��Ѿ���, ���׳��쳣
     * @throws OutOfLengthException ����Ѵ��ļ�����������ų�����쳣
     */
    public String readFile(String name) throws FileAlreadyOpenException, OutOfLengthException {
        OpenFileTable openFileTable = OpenFileTableView.get(name);
        FileEntry fileEntry;
        if(openFileTable == null){
            fileEntry = openFile(name, 1);
        }else{
            fileEntry = openFileTable.getFileEntry();
        }
        // ����򿪲��ˣ��򷵻�null
        if(fileEntry == null){
            return null;
        }
        FAT fat = FAT.getInstance();
        Disk disk = Disk.getInstance();
        // ���ļ���ӵ�еĴ��̿��
        LinkedList<Integer> queue = new LinkedList<>();
        int blockIndex = fileEntry.getStartBlockIndex();
        queue.addLast(blockIndex);
        int nextIndex = fat.getContent(blockIndex);
        while(nextIndex != -1){
            queue.offer(nextIndex);
            nextIndex = fat.getContent(nextIndex);
        }
        StringBuilder builder = new StringBuilder("");
        byte[] buffer = new byte[64];
        while(!queue.isEmpty()){
            blockIndex = queue.removeFirst();
            buffer = disk.readBlock(blockIndex);
            for(byte temp : buffer){
                char end = (char) temp;
                if(end == END_FILE_FLAG){
                    return builder.toString();
                }
                builder.append(end);
            }
        }
        return builder.toString();
    }

    /**
     * @param content Ҫд�������
     * @param name    �ļ���(����׺)
     * @return д���Ƿ�ɹ�
     */
    public boolean writeFile(String content, String name){
        String[] nameArray = name.split("\\.");
        if(nameArray.length != 2){
            return false;
        }
        // ����ļ�û�д򿪵Ļ���������д��
        OpenFileTable openFileTable = OpenFileTableView.get(name);
        if(openFileTable == null){
            return false;
        }
        FAT fat = FAT.getInstance();
        Disk disk = Disk.getInstance();
        FileEntry entry = openFileTable.getFileEntry();
        byte[][] byteContent = splitByteArray(content.getBytes());
        // �ļ���ӵ�еĿ�
        LinkedList<Integer> fileBlocks = new LinkedList<>();
        int blockIndex = entry.getStartBlockIndex();
        fileBlocks.addLast(blockIndex);
        blockIndex = fat.getContent(blockIndex);
        while(blockIndex != -1){
            fileBlocks.addLast(blockIndex);
            blockIndex = fat.getContent(blockIndex);
        }

        for(Integer i : fileBlocks){
            fullEndFileFlag(i);
        }

        // �������Ҫ�Ŀռ䲻�㣬������µ�
        int needBlock = byteContent.length;
        int hasBlock = fileBlocks.size();
        if(needBlock > hasBlock) {
            int lastIndex = fileBlocks.getLast();
            while (needBlock - hasBlock > 0) {
                lastIndex = fat.allocation(lastIndex);
                // ����ļ�������־
                fullEndFileFlag(lastIndex);
                fileBlocks.addLast(lastIndex);
                needBlock--;
            }
        }
        for(int i = 0; i < byteContent.length; i++){
            int index = fileBlocks.removeFirst();
            disk.writeBlock(index, byteContent[i].length, byteContent[i]);
        }
        return true;
    }


    /**
     * ��array�зֳɶ�ά����
     * ûһ��ά�����64���ֽ�
     * @param array
     * @return
     */
    private byte[][] splitByteArray(byte[] array){
        int rows = array.length / 64 + 1;
        byte[][] content = new byte[rows][];
        int readNum = 0;
        for(int i = 0; i < content.length; i++){
            int sub = array.length - readNum;
            if(sub >= 64){
                content[i] = new byte[64];
            }else{
                content[i] = new byte[sub];
            }
            for(int j = 0; j < content[i].length; j++, readNum++){
                content[i][j] = array[readNum];
            }
        }
        return content;
    }
    /**
     * ����ļ�������־
     * @param blockIndex
     */
    private void fullEndFileFlag(int blockIndex){
        Disk disk = Disk.getInstance();
        byte[] buffer = new byte[64];
        for(int i = 0; i < buffer.length; i++){
            buffer[i] = END_FILE_FLAG;
        }
        disk.writeBlock(blockIndex, buffer);
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

    public Entry[] getEntries() {
        return entries;
    }

    public void setEntries(Entry[] entries) {
        this.entries = entries;
    }
}
