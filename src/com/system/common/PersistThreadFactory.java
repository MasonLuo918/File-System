package com.system.common;

import java.util.concurrent.ThreadFactory;

public class PersistThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        // ����Ϊ������ȼ�����̨����
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    }
}
