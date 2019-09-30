package com.system.model;

/**
 * 文件分配表
 * @author masonluo
 */
public class FAT {

    private volatile static FAT fat = null;

    // 文件分配表的长度
    private static final int LENGTH = 128;

    /*
     *   字节表, 索引范围为0 - 127, 当table[index] = 0 的时候，表示磁盘块空闲,
     *   当table[index] = -1 的时候，表示文件结束,其中，磁盘块第一第二块存储fat表,为-1
     */
    private byte[] table = new byte[LENGTH];

    private FAT() {
    }

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
     * 分配一个磁盘块, 如果有空余的磁盘快，则分配给文件，然后将fat上对应的位置置1
     * 如果没有空余的磁盘块，则返回-1
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