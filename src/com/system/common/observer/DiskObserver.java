package com.system.common.observer;
import com.system.model.Disk;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiskObserver implements Observer {

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    @Override
    public void update(Observable o, Object arg) {
        executor.execute(new Worker());
    }

    class Worker implements Runnable{

        @Override
        public void run() {
            Disk.getInstance().persistDisk();
        }
    }
}
