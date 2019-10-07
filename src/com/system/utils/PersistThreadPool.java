package com.system.utils;

import com.system.common.PersistThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersistThreadPool {
    private static ExecutorService executors = Executors.newFixedThreadPool(2, new PersistThreadFactory());

    public static void submit(Runnable runnable){
        executors.submit(runnable);
    }
}
