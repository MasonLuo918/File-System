package com.system.utils;

import com.system.exception.DiskCapacityErrorException;
//import sun.jvm.hotspot.runtime.Bytes;

import java.io.*;

public class FileUtils {

    /**
     * 保存字节数组进文件中
     * @param bytes 要保存的字节数组
     * @param file  要保存的文件
     * @throws IOException
     */
    public static void saveBytes(byte[][] bytes, File file) throws IOException {
        FileOutputStream fos =  new FileOutputStream(file);
        for(int i = 0; i < bytes.length; i++){
            fos.write(bytes[i], 0, bytes[i].length);
        }
    }

    /**
     * 将文件中的数据读出来，存放入bytes数组中
     * @param bytes 要存放的bytes数组
     * @param file  读取的文件
     * @throws IOException
     * @throws DiskCapacityErrorException 如果磁盘的容量不对，抛出这个异常
     */
    public static void loadBytes(byte[][] bytes, File file) throws IOException, DiskCapacityErrorException {
        FileInputStream fis = new FileInputStream(file);
        for(int i = 0; i < bytes.length; i++){
            int count = fis.read(bytes[i], 0, bytes[i].length);
            if(count != bytes[i].length){
                throw new DiskCapacityErrorException();
            }
        }
    }
}
