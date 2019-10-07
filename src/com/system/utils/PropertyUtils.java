package com.system.utils;

public class PropertyUtils {
    public static boolean isFolder(byte num){
        return !((num&8) == 0);
    }
}
