package com.system.model;

import com.system.utils.PersistThreadPool;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * �ļ������
 * @author masonluo
 */
public class FAT {

    private volatile static FAT fat = null;

    // �ļ������ĳ���
    private static final int LENGTH = 128;

    // ��һ�������Ĵ��̿飬
    private int lastIndex = 2;

    /*
     *   �ֽڱ�, ������ΧΪ0 - 127, ��table[index] = 0 ��ʱ�򣬱�ʾ���̿����,
     *   ��table[index] = -1 ��ʱ�򣬱�ʾ�ļ�����,���У����̿��һ�ڶ���洢fat��,Ϊ-1
     */
    private byte[] table = new byte[LENGTH];

    // ά��һ�����п��б�
    private LinkedList<Integer> emptyBlockIndexList = new LinkedList<>();

    // �̳߳�
    private ExecutorService executor = Executors.newCachedThreadPool();

    private FAT() {
        Disk disk = Disk.getInstance();
        loadFAT(disk);
    }
    /**
     * ��ȡFATʵ��
     * @return fatʵ��
     */
    public static FAT getInstance() {
        // ˫���������ƣ�ȷ���̰߳�ȫ��ֻ������һ��fat
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
     * ����һ���� (һ���ļ�׷���ļ�������)
     * @param lastBlockIndex ������Ѿ���һ����Ļ�����Ҫ������һ�����������Ȼ�������ļ������
     * @return ������̿黹û�������򷵻ؿɷ���������ţ����򣬷���-1
     */
    public int allocation(int lastBlockIndex){
        if(lastBlockIndex > table.length){
            throw new RuntimeException("index error");
        }
        int index;
        synchronized (FAT.class){
            // ���û�п��еĿ飬����ʧ��
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
     * ����һ���飨�״η����ļ��������
     * @return �ɷ���Ĵ��̿���±�
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
     * ����һ����
     * @param blockIndex Ҫ���յĿ�����
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
     * �Ӵ��̿��м���fat
     * @param disk ���̿�
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
     * �־û�FAT����̣�ʹ�������ܹ���ʱͬ����������
     */
    private void persistFAT(){
        PersistThreadPool.submit(new Worker());
    }
    /**
     * ��ʼ��һ�����п����
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