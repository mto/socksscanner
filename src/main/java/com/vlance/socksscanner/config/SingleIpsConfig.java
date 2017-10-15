package com.vlance.socksscanner.config;

import com.vlance.socksscanner.spa.SocksProxyAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/15/17
 */
public class SingleIpsConfig implements Iterator<SocksProxyAddress> {

    private final static Logger LOGGER = LoggerFactory.getLogger(SingleIpsConfig.class);

    private BufferedReader buf;

    private LinkedList<SocksProxyAddress> queue = new LinkedList<>();

    private final AtomicInteger counter = new AtomicInteger();

    public SingleIpsConfig() {
        this(false);
    }

    public SingleIpsConfig(boolean readAll) {
        try {
            LOGGER.info("Reading config from single_ips.txt");
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("single_ips.txt");
            buf = new BufferedReader(new InputStreamReader(is));

            if(readAll){
                readAll();
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to read config from single_ips.txt", ex);
        }
    }

    @Override
    public SocksProxyAddress next() {
        return queue.removeFirst();
    }

    @Override
    public boolean hasNext() {
        if (queue.isEmpty()) {
            readMore();
        }
        return !queue.isEmpty();
    }

    private String readMore() {
        String line = null;
        try {
            line = buf.readLine();
            if (line != null) {
                String[] tokens = line.split(":");
                if (tokens.length == 2) {
                    String ip = tokens[0].trim();
                    int port = Integer.parseInt(tokens[1].trim());
                    queue.add(new SocksProxyAddress(ip, port));
                    counter.addAndGet(1);
                }
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception when reading single_ips.txt", ioEx);
        }

        return line;
    }

    public void readAll() {
        String line = readMore();
        while (line != null) {
            line = readMore();
        }
    }

    public int getFinalCount() {
        return counter.get();
    }
}
