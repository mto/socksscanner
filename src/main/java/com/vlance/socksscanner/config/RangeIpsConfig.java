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
public class RangeIpsConfig implements Iterator<SocksProxyAddress> {

    private final static Logger LOGGER = LoggerFactory.getLogger(RangeIpsConfig.class);

    private BufferedReader buf;

    private LinkedList<SocksProxyAddress> queue = new LinkedList<>();

    private AtomicInteger counter = new AtomicInteger(0);

    public RangeIpsConfig() {
        this(false);
    }

    public RangeIpsConfig(boolean readAll) {
        try {
            LOGGER.info("Reading config from range_ips.txt");
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("range_ips.txt");
            buf = new BufferedReader(new InputStreamReader(is));

            if(readAll){
                readAll();
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to read config from range_ips.txt", ex);
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
                String[] tokens = line.split(" ");
                if (tokens.length == 4) {
                    String firstIp = tokens[0].trim();
                    int nbHosts = Integer.parseInt(tokens[2].trim().substring(1));

                    int lastDotIdx = firstIp.lastIndexOf('.');
                    String ipPart1 = firstIp.substring(0, lastDotIdx);
                    int ipLastNumber = Integer.parseInt(firstIp.substring(lastDotIdx + 1));

                    for (int j = 0; j < nbHosts; j++) {
                        queue.add(new SocksProxyAddress(ipPart1 + "." + (ipLastNumber + j), 1080));
                    }
                    counter.addAndGet(nbHosts);
                }
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception when reading range_ips.txt", ioEx);
        }

        return line;
    }

    public void readAll(){
        String line = readMore();
        while(line != null){
            line = readMore();
        }
    }

    public int getFinalCount() {
        return counter.get();
    }
}
