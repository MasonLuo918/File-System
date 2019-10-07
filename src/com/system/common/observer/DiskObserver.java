package com.system.common.observer;
import com.system.model.Disk;
import com.system.utils.PersistThreadPool;

import java.util.Observable;
import java.util.Observer;

public class DiskObserver implements Observer {


    @Override
    public void update(Observable o, Object arg) {
        PersistThreadPool.submit(new Worker());
    }

    class Worker implements Runnable{
        @Override
        public void run() {
            Disk.getInstance().persistDisk();
        }
    }
}
