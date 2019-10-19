package com.system.utils;

import com.system.exception.DiskCapacityErrorException;
//import sun.jvm.hotspot.runtime.Bytes;

import java.io.*;

public class FileUtils {

    /**
     * �����ֽ�������ļ���
     * @param bytes Ҫ������ֽ�����
     * @param file  Ҫ������ļ�
     * @throws IOException
     */
    public static void saveBytes(byte[][] bytes, File file) throws IOException {
        FileOutputStream fos =  new FileOutputStream(file);
        for(int i = 0; i < bytes.length; i++){
            fos.write(bytes[i], 0, bytes[i].length);
        }
    }

    /**
     * ���ļ��е����ݶ������������bytes������
     * @param bytes Ҫ��ŵ�bytes����
     * @param file  ��ȡ���ļ�
     * @throws IOException
     * @throws DiskCapacityErrorException ������̵��������ԣ��׳�����쳣
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
