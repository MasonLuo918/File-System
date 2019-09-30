package com.system.model;

import com.system.common.FileUtils;
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

    private static final long serialVersionUID = 1L;

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
        persistDisk(file);
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
    /**
     * 通知观察者，磁盘已经发生变化
     */
    public void notifyObserver(){
        setChanged();
        notifyObservers();
    }
    /**
     * 将space的数据持久化到一个文件中
     * @param file
     */
    public void persistDisk(File file) {
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
     * @param i 索引值
     * @return 磁盘块的复制，避免外部直接操纵磁盘
     */
    private byte[] getBlock(int i){
        // 不能获取文件分配表、不能超出范围
        if(i < 2 || i > BLOCK_SIZE){
            throw new RuntimeException("获取磁盘快错误");
        }
        // 传回去一个复制了的数组，避免外部直接操纵磁盘的数组
        return Arrays.copyOf(space[i], space[i].length);
    }

    public static void main(String[] args) throws FileNotFoundException {
        Disk.getInstance();
    }
}
