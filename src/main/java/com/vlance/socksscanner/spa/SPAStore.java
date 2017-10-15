package com.vlance.socksscanner.spa;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/10/17
 */
public class SPAStore {

    private final ConcurrentLinkedQueue<SocksProxyAddress> spaQueue = new ConcurrentLinkedQueue<SocksProxyAddress>();

    private final LinkedList<SocksProxyAddress> filteredSpa = new LinkedList<>();

    private final LinkedList<SocksProxyAddress> failedSpa = new LinkedList<>();

    private AtomicInteger counter = new AtomicInteger(0);

    private final List<Runnable> reachMaxSizeCallbacks = new LinkedList<>();

    private final ExecutorService storeWorker;

    private int maxSize;

    public SPAStore() {
        storeWorker = Executors.newFixedThreadPool(5, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);

                return t;
            }
        });
    }

    public void add(SocksProxyAddress spa) {
        spaQueue.add(spa);
        filteredSpa.add(spa);

        if (counter.addAndGet(1) >= maxSize) {
            triggerReachMaxSizeCallbacks();
        }
        System.out.println("Number of scanned hosts: " + counter.get());
    }

    public void addFailedSpa(SocksProxyAddress spa) {
        failedSpa.add(spa);
        if (counter.addAndGet(1) >= maxSize) {
            triggerReachMaxSizeCallbacks();
        }
        System.out.println("Number of scanned hosts: " + counter.get());
    }

    public SocksProxyAddress poll() {
        return spaQueue.poll();
    }

    public void record(File f) {
        try {
            BufferedWriter buff = new BufferedWriter(new FileWriter(f));
            for (SocksProxyAddress spa : filteredSpa) {
                buff.write(spa.host + ":" + spa.port + "\n");
            }
            buff.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void addReachMaxSizeCallback(Runnable r) {
        reachMaxSizeCallbacks.add(r);
    }

    public void setMaxSize(int msize) {
        this.maxSize = msize;
    }

    public void triggerReachMaxSizeCallbacks() {
        for (Runnable r : reachMaxSizeCallbacks) {
            storeWorker.submit(r);
        }
    }

}
