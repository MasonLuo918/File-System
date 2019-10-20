package com.system.common;

import java.util.concurrent.ThreadFactory;

public class PersistThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        // 设置为最低优先级，后台保存
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    }
}
