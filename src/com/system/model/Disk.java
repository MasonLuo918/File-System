package com.system.model;

import com.system.common.observer.DiskObserver;
import com.system.utils.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 双重锁检测机制，确保只有一个类生成e
 * 观察者模式设计模式，如果磁盘有所变化，
 * 则通知观察者，观察者可调用addObserver
 * 方法成为一个观察者
 * @author masonluo
 */
public class Disk extends Observable {

    /*    1、分配内存空间
          2、初始化对象
          3、赋值
          以上的步骤可能会被打乱，所以需要使用volatile限制冲排序
    */
    private volatile static Disk instance = null;

    // 读写锁
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Lock readLock = rwLock.readLock();
    private Lock writeLock = rwLock.writeLock();

    private static final String FILE_PATH = "Disk.disk";

    // 磁盘块的大小
    private static final int BLOCK_SIZE = 64;

    // 磁盘块的个数
    private static final int BLOCK_NUM = 128;

    // 文件结束辨识符号
    private static final byte END_FILE_MARK = (byte) 255;

    // 物理快可使用标识符
    private static final byte USABLE = (byte) 0;

    // 磁盘实际存储空间，默认为128个块，每个块大小为64B
    private byte[][] space;

    private Disk() {
        File file = new File(FILE_PATH);
        // 如果磁盘文件为空，说明磁盘是首次使用，需要初始化
        if (file.length() == 0) {
            initializeDisk(file);
        }
        // 加载文件内容
        loadDisk(file);
        loadObserver();
        notifyObserver();
    }
    /**
     * @return 一个磁盘的实例
     */
    public static Disk getInstance() {
        if (instance == null) {
            synchronized (Disk.class) {
                if (instance == null) {
                    instance = new Disk();
                }
            }
        }
        return instance;
    }
    /**
     * 通知观察者，磁盘已经发生变化
     */
    public void notifyObserver(){
        setChanged();
        notifyObservers();
    }
    /**
     * 将space的数据持久化到文件中
     */
    public void persistDisk() {
        File file = new File(FILE_PATH);
        try {
            FileUtils.saveBytes(space, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
    }
    /**
     * 从一个文件加载磁盘
     * @param file
     */
    public void loadDisk(File file){
        space = new byte[BLOCK_NUM][BLOCK_SIZE];
        try{
            FileUtils.loadBytes(space, file);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 获取一个索引值为i的磁盘块
     * @param index 索引值
     * @return 磁盘块的复制，避免外部直接操纵磁盘
     */
    public byte[] readBlock(int index){
        if(index > BLOCK_NUM){
            throw new IndexOutOfBoundsException("index error");
        }
        // 传回去一个复制了的数组，避免外部直接操纵磁盘的数组
        readLock.lock();
        try{
            return Arrays.copyOf(space[index], space[index].length);
        }finally {
            readLock.unlock();
        }
    }
    /**
     * 写一个磁盘块
     * @param index 索引值
     * @param bytes 要写入的内容
     */
    public void writeBlock(int index, byte[] bytes){
        if(index > BLOCK_NUM || bytes.length > BLOCK_SIZE){
            throw new IndexOutOfBoundsException("index error");
        }
        writeLock.lock();
        try{
            // 避免外部的数组直接操纵磁盘
            space[index] = Arrays.copyOf(bytes, bytes.length);
            // 通知观察者，磁盘变化
            notifyObserver();
        }finally {
            writeLock.unlock();
        }
    }

    /**
     * 写一个磁盘块， 从index的第0个字节，写入writeNum个字节，对应bytes的第0个，
     * 到第writeNum个字节
     * @param index 磁盘块索引
     * @param writeNum 写入的个数
     * @param bytes 要写入的字节数
     */
    public void writeBlock(int index, int writeNum,  byte[] bytes){
        if(writeNum > BLOCK_SIZE){
            throw new IndexOutOfBoundsException("index should be less than " + BLOCK_SIZE);
        }
        writeLock.lock();
        try{
            for(int i = 0; i < writeNum; i++){
                space[index][i] = bytes[i];
            }
        }finally {
            writeLock.unlock();
            notifyObserver();
        }
    }

    /**
     * 写入一个磁盘块
     * @param index 写入的磁盘块的索引
     * @param startIndex 写入的第startIndex个字节
     * @param endIndex 写入截止到第endIndex个字节（不包括endIndex）
     * @param bytes 要写入的数据
     */
    public void writeBlock(int index, int startIndex, int endIndex, byte[] bytes){
        if(bytes.length > endIndex - startIndex){
            throw new RuntimeException("The length of byte array does not match");
        }
        if(endIndex > BLOCK_SIZE){
            throw new IndexOutOfBoundsException("endIndex should be less than " + BLOCK_SIZE);
        }
        writeLock.lock();
        try{
            for(int i = startIndex; i < endIndex; i++){
                space[index][i] = bytes[i - startIndex];
            }
        }finally {
            writeLock.unlock();
            notifyObserver();
        }

    }
    /**
     * 如果判断文件为空，执行磁盘的首次初始化，初始化byte数组，
     * 并且将其写入文件中
     *
     * @param file 实际存储的磁盘文件
     */
    private void initializeDisk(File file) {
        if (file.length() != 0) {
            throw new RuntimeException("file length should be 0!");
        }
        space = new byte[BLOCK_NUM][BLOCK_SIZE];
        for (int i = 0; i < space.length; i++) {
            space[i] = new byte[BLOCK_SIZE];
        }

        for (int i = 0; i < space.length; i++) {
            for (int j = 0; j < space[i].length; j++) {
                space[i][j] = (byte) 255;
            }
        }
        initializeFAT();
        persistDisk();
    }
    /**
     * 初始化FAT文件分配表
     */
    private void initializeFAT() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < space[i].length; j++) {
                // 标示为0说明未使用
                space[i][j] = USABLE;
            }
        }
        // 将盘块0和1设置为已使用
        space[0][0] = END_FILE_MARK;
        space[0][1] = END_FILE_MARK;
    }
    private void loadObserver(){
        DiskObserver observer = new DiskObserver();
        addObserver(observer);
    }
    public static int getBlockSize() {
        return BLOCK_SIZE;
    }

    public static int getBlockNum() {
        return BLOCK_NUM;
    }

    public static byte getEndFileMark() {
        return END_FILE_MARK;
    }

    public static byte getUSABLE() {
        return USABLE;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Disk.getInstance();
    }
}
