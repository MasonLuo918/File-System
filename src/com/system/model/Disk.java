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
 * ˫���������ƣ�ȷ��ֻ��һ��������e
 * �۲���ģʽ���ģʽ��������������仯��
 * ��֪ͨ�۲��ߣ��۲��߿ɵ���addObserver
 * ������Ϊһ���۲���
 * @author masonluo
 */
public class Disk extends Observable {

    private static final long serialVersionUID = 1L;

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
        persistDisk(file);
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
    /**
     * ֪ͨ�۲��ߣ������Ѿ������仯
     */
    public void notifyObserver(){
        setChanged();
        notifyObservers();
    }
    /**
     * ��space�����ݳ־û���һ���ļ���
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
     * @param i ����ֵ
     * @return ���̿�ĸ��ƣ������ⲿֱ�Ӳ��ݴ���
     */
    private byte[] getBlock(int i){
        // ���ܻ�ȡ�ļ���������ܳ�����Χ
        if(i < 2 || i > BLOCK_SIZE){
            throw new RuntimeException("��ȡ���̿����");
        }
        // ����ȥһ�������˵����飬�����ⲿֱ�Ӳ��ݴ��̵�����
        return Arrays.copyOf(space[i], space[i].length);
    }

    public static void main(String[] args) throws FileNotFoundException {
        Disk.getInstance();
    }
}
