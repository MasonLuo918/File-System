package com.system.model;

import com.system.utils.PersistThreadPool;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 文件分配表
 * @author masonluo
 */
public class FAT {

    private volatile static FAT fat = null;

    // 文件分配表的长度
    private static final int LENGTH = 128;

    // 上一次搜索的磁盘块，
    private int lastIndex = 2;

    /*
     *   字节表, 索引范围为0 - 127, 当table[index] = 0 的时候，表示磁盘块空闲,
     *   当table[index] = -1 的时候，表示文件结束,其中，磁盘块第一第二块存储fat表,为-1
     */
    private byte[] table = new byte[LENGTH];

    // 维护一个空闲块列表
    private LinkedList<Integer> emptyBlockIndexList = new LinkedList<>();

    // 线程池
    private ExecutorService executor = Executors.newCachedThreadPool();

    private FAT() {
        Disk disk = Disk.getInstance();
        loadFAT(disk);
    }
    /**
     * 获取FAT实例
     * @return fat实例
     */
    public static FAT getInstance() {
        // 双重锁检测机制，确保线程安全，只能生成一个fat
        if (fat == null) {
            synchronized (FAT.class) {
                if (fat == null) {
                    fat = new FAT();
                }
            }
        }
        return fat;
    }
    /**
     * 分配一个块 (一个文件追加文件块的情况)
     * @param lastBlockIndex 如果是已经有一个块的话，需要给出上一个块的索引，然后链接文件分配表
     * @return 如果磁盘块还没有满，则返回可分配的索引号，否则，返回-1
     */
    public int allocation(int lastBlockIndex){
        if(lastBlockIndex > table.length){
            throw new RuntimeException("index error");
        }
        int index;
        synchronized (FAT.class){
            // 如果没有空闲的块，分配失败
            if(emptyBlockIndexList.isEmpty()){
                return -1;
            }
            index = emptyBlockIndexList.removeFirst();
            table[lastBlockIndex] = (byte) index;
            table[index] = -1;
            persistFAT();
            return index;
        }
    }
    /**
     * 分配一个块（首次分配文件块情况）
     * @return 可分配的磁盘块的下标
     */
    public int allocation(){
        synchronized (FAT.class){
            if(emptyBlockIndexList.isEmpty()){
                return -1;
            }
            int index = emptyBlockIndexList.removeFirst();
            table[index] = -1;
            persistFAT();
            return index;
        }
    }
    /**
     * 回收一个块
     * @param blockIndex 要回收的块索引
     */
    public void collect(int blockIndex){
        if(blockIndex < 2 || blockIndex > table.length){
            throw new RuntimeException("index error");
        }
        synchronized (FAT.class){
            if(table[blockIndex] != 0){
                table[blockIndex] = 0;
                emptyBlockIndexList.addLast(blockIndex);
                persistFAT();
            }
        }
    }

    public int getContent(int index){
        return table[index];
    }

    /**
     * 从磁盘块中加载fat
     * @param disk 磁盘块
     */
    private void loadFAT(Disk disk){
        byte[] block;
        for(int i = 0; i < 2; i++){
            block = disk.readBlock(i);
            for(int j = 0; j < block.length; j++){
                table[i * block.length + j] = block[j];
            }
        }
        initializeEmptyBlockList();
    }
    /**
     * 持久化FAT入磁盘，使得数组能够及时同步到磁盘中
     */
    private void persistFAT(){
        PersistThreadPool.submit(new Worker());
    }
    /**
     * 初始化一个空闲块队列
     */
    private void initializeEmptyBlockList(){
        for(int i = 0; i < table.length; i++){
            if(table[i] == 0){
                emptyBlockIndexList.addLast(i);
            }
        }
    }

     class Worker implements Runnable{
        @Override
        public void run() {
            byte[] tempByte = Arrays.copyOfRange(table, 0, table.length / 2);
            Disk.getInstance().writeBlock(0, tempByte);
            tempByte = Arrays.copyOfRange(table, table.length / 2, table.length);
            Disk.getInstance().writeBlock(1, tempByte);
        }
    }
}