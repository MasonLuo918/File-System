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
 * ˫���������ƣ�ȷ��ֻ��һ��������e
 * �۲���ģʽ���ģʽ��������������仯��
 * ��֪ͨ�۲��ߣ��۲��߿ɵ���addObserver
 * ������Ϊһ���۲���
 * @author masonluo
 */
public class Disk extends Observable {

    /*    1�������ڴ�ռ�
          2����ʼ������
          3����ֵ
          ���ϵĲ�����ܻᱻ���ң�������Ҫʹ��volatile���Ƴ�����
    */
    private volatile static Disk instance = null;

    // ��д��
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Lock readLock = rwLock.readLock();
    private Lock writeLock = rwLock.writeLock();

    private static final String FILE_PATH = "Disk.disk";

    // ���̿�Ĵ�С
    private static final int BLOCK_SIZE = 64;

    // ���̿�ĸ���
    private static final int BLOCK_NUM = 128;

    // �ļ�������ʶ����
    private static final byte END_FILE_MARK = (byte) 255;

    // ������ʹ�ñ�ʶ��
    private static final byte USABLE = (byte) 0;

    // ����ʵ�ʴ洢�ռ䣬Ĭ��Ϊ128���飬ÿ�����СΪ64B
    private byte[][] space;

    private Disk() {
        File file = new File(FILE_PATH);
        // ��������ļ�Ϊ�գ�˵���������״�ʹ�ã���Ҫ��ʼ��
        if (file.length() == 0) {
            initializeDisk(file);
        }
        // �����ļ�����
        loadDisk(file);
        loadObserver();
        notifyObserver();
    }
    /**
     * @return һ�����̵�ʵ��
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
     * ֪ͨ�۲��ߣ������Ѿ������仯
     */
    public void notifyObserver(){
        setChanged();
        notifyObservers();
    }
    /**
     * ��space�����ݳ־û����ļ���
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
     * ��һ���ļ����ش���
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
     * ��ȡһ������ֵΪi�Ĵ��̿�
     * @param index ����ֵ
     * @return ���̿�ĸ��ƣ������ⲿֱ�Ӳ��ݴ���
     */
    public byte[] readBlock(int index){
        if(index > BLOCK_NUM){
            throw new IndexOutOfBoundsException("index error");
        }
        // ����ȥһ�������˵����飬�����ⲿֱ�Ӳ��ݴ��̵�����
        readLock.lock();
        try{
            return Arrays.copyOf(space[index], space[index].length);
        }finally {
            readLock.unlock();
        }
    }
    /**
     * дһ�����̿�
     * @param index ����ֵ
     * @param bytes Ҫд�������
     */
    public void writeBlock(int index, byte[] bytes){
        if(index > BLOCK_NUM || bytes.length > BLOCK_SIZE){
            throw new IndexOutOfBoundsException("index error");
        }
        writeLock.lock();
        try{
            // �����ⲿ������ֱ�Ӳ��ݴ���
            space[index] = Arrays.copyOf(bytes, bytes.length);
            // ֪ͨ�۲��ߣ����̱仯
            notifyObserver();
        }finally {
            writeLock.unlock();
        }
    }

    /**
     * дһ�����̿飬 ��index�ĵ�0���ֽڣ�д��writeNum���ֽڣ���Ӧbytes�ĵ�0����
     * ����writeNum���ֽ�
     * @param index ���̿�����
     * @param writeNum д��ĸ���
     * @param bytes Ҫд����ֽ���
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
     * д��һ�����̿�
     * @param index д��Ĵ��̿������
     * @param startIndex д��ĵ�startIndex���ֽ�
     * @param endIndex д���ֹ����endIndex���ֽڣ�������endIndex��
     * @param bytes Ҫд�������
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
     * ����ж��ļ�Ϊ�գ�ִ�д��̵��״γ�ʼ������ʼ��byte���飬
     * ���ҽ���д���ļ���
     *
     * @param file ʵ�ʴ洢�Ĵ����ļ�
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
     * ��ʼ��FAT�ļ������
     */
    private void initializeFAT() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < space[i].length; j++) {
                // ��ʾΪ0˵��δʹ��
                space[i][j] = USABLE;
            }
        }
        // ���̿�0��1����Ϊ��ʹ��
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
