package com.system.model;

/**
 * �ļ������
 * @author masonluo
 */
public class FAT {

    private volatile static FAT fat = null;

    // �ļ������ĳ���
    private static final int LENGTH = 128;

    /*
     *   �ֽڱ�, ������ΧΪ0 - 127, ��table[index] = 0 ��ʱ�򣬱�ʾ���̿����,
     *   ��table[index] = -1 ��ʱ�򣬱�ʾ�ļ�����,���У����̿��һ�ڶ���洢fat��,Ϊ-1
     */
    private byte[] table = new byte[LENGTH];

    private FAT() {
    }

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
     * ����һ�����̿�, ����п���Ĵ��̿죬�������ļ���Ȼ��fat�϶�Ӧ��λ����1
     * ���û�п���Ĵ��̿飬�򷵻�-1
     * @return
     */
    public int allocation(){
        for(int i = 0; i < table.length; i++){
            if(table[i] != 0){
                table[i] = -1;
                return i;
            }
        }
        return -1;
    }
}