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
 * 文件夹，将一个盘块读取成一个文件夹，方便操作
 */
public class Folder {

    private static final char END_FILE_FLAG = 0;

    private static final byte[] EMPTY_ENTRY = new byte[]{
            '$','$','$','$','$','$','$','$'
    };

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
     * @param name       该目录的名字
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
    public Folder(FolderEntry folderEntry) {
        this(folderEntry.getStartBlockIndex(), folderEntry.getName());
        myself = folderEntry;
    }

    /**
     * 创建一个文件夹
     *
     * @param name 文件名
     * @return 创建成功返回文件加Entry，否则返回NULL
     */
    public FolderEntry createFolder(String name) {
        return createFolder(name, false);
    }

    /**
     * 创建一个文件夹
     *
     * @param name     文件名
     * @param readOnly 文件夹属性，是否只读
     * @return 创建成功返回文件加Entry，否则返回NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly) {
        return createFolder(name, readOnly, false);
    }

    /**
     * 创建一个文件夹
     *
     * @param name     文件名
     * @param readOnly 文件夹属性，是否只读
     * @param system   文件夹属性，是否系统文件
     * @return 创建成功返回文件加Entry，否则返回NULL
     */
    public FolderEntry createFolder(String name, boolean readOnly, boolean system) {
        return createFolder(name, readOnly, system, true);
    }

    /**
     * 创建一个文件夹
     *
     * @param name     文件名
     * @param readOnly 文件夹属性，是否只读
     * @param system   文件夹属性，是否系统文件
     * @param normal   文件夹属性，是否普通文件
     * @return 创建成功返回文件加Entry，否则返回NULL
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
        // 删除
        deleteEntry(entry);
        // 回收
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
     * 添加一个Entry，并且写入磁盘中
     *
     * @param entry 要添加的entry
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
     * 删除一个entry
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
     * 初始化一个文件夹，将其目录项全部置空
     *
     * @param folderEntry 文件夹Entry
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
     * 获取一个指定名字的Entry
     *
     * @param name entry名字
     * @return 对应的entry，如果不存在则返回null
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
     * 文件夹是否满了
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
     * 文件夹是否为空
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
     * 是否含有该文件
     *
     * @param name 文件entry名
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
     * 加载一个文件夹
     *
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
                if (PropertyUtils.isFolder(temp[5])) {
                    entries[i] = new FolderEntry(temp);
                } else {
                    entries[i] = new FileEntry(temp);
                }
            }
        }
    }

    /**
     * 创建一个文件
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
     * 新建一个文件
     *
     * @param name     文件名
     * @param readOnly 是否只读
     * @param normal   普通文件
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
     * 打开文件操作
     * @param name 文件名称
     * @param flag 0为读，1为写（默认读写一体,flag=1）
     * @throws Exception 如果文件已经打开，则抛出异常
     * @return
     * 如果不能打开，则返回null(已打开文件操作表已满或者文件名称错误)
     * 如果成果打开，返回对应的entry
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
            throw new OutOfLengthException("已打开文件分配表已满，无法继续打开文件");
        }
        OpenFileTable openFileTable = new OpenFileTable(fileEntry, name, fileEntry.getPropertyForByte(), fileEntry.getStartBlockIndex(), fileEntry.getLength(), flag);
        boolean success = OpenFileTableView.add(openFileTable);
        if(success){
            return fileEntry;
        }
        return null;
    }

    /**
     * @param name 文件名
     * @return 返回文件的string字符串
     * @throws FileAlreadyOpenException 如果文件已经打开, 则抛出异常
     * @throws OutOfLengthException 如果已打开文件表过长，则排除这个异常
     */
    public String readFile(String name) throws FileAlreadyOpenException, OutOfLengthException {
        OpenFileTable openFileTable = OpenFileTableView.get(name);
        FileEntry fileEntry;
        if(openFileTable == null){
            fileEntry = openFile(name, 1);
        }else{
            fileEntry = openFileTable.getFileEntry();
        }
        // 如果打开不了，则返回null
        if(fileEntry == null){
            return null;
        }
        FAT fat = FAT.getInstance();
        Disk disk = Disk.getInstance();
        // 该文件所拥有的磁盘块号
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
     * @param content 要写入的内容
     * @param name    文件名(带后缀)
     * @return 写入是否成功
     */
    public boolean writeFile(String content, String name){
        String[] nameArray = name.split("\\.");
        if(nameArray.length != 2){
            return false;
        }
        // 如果文件没有打开的话，不允许写入
        OpenFileTable openFileTable = OpenFileTableView.get(name);
        if(openFileTable == null){
            return false;
        }
        FAT fat = FAT.getInstance();
        Disk disk = Disk.getInstance();
        FileEntry entry = openFileTable.getFileEntry();
        byte[][] byteContent = splitByteArray(content.getBytes());
        // 文件所拥有的块
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

        // 如果所需要的空间不足，则分配新的
        int needBlock = byteContent.length;
        int hasBlock = fileBlocks.size();
        if(needBlock > hasBlock) {
            int lastIndex = fileBlocks.getLast();
            while (needBlock - hasBlock > 0) {
                lastIndex = fat.allocation(lastIndex);
                // 填充文件结束标志
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
     * 将array切分成二维数组
     * 没一个维度最大64个字节
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
     * 填充文件结束标志
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
