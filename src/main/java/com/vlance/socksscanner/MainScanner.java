package com.vlance.socksscanner;

import com.vlance.socksscanner.config.RangeIpsConfig;
import com.vlance.socksscanner.config.SingleIpsConfig;
import com.vlance.socksscanner.spa.SPAStore;
import com.vlance.socksscanner.spa.SocksProxyAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/10/17
 */
public class MainScanner {

    private final static Logger LOGGER = LoggerFactory.getLogger(MainScanner.class);

    public MainScanner() {
    }

    public void scanSocks5RangeIps() {
        SPAStore socksStore = new SPAStore();
        TCPSocksScanner tcpSocksScanner = new TCPSocksScanner(10, socksStore);
        RangeIpsConfig ripsCf = new RangeIpsConfig(true);
        int nb = ripsCf.getFinalCount();

        final long startTime = System.currentTimeMillis();
        socksStore.addReachMaxSizeCallback(new Runnable() {
            @Override
            public void run() {
                long tmp = System.currentTimeMillis();
                LOGGER.info("Total scanning time: " + (tmp - startTime));

                tcpSocksScanner.shutdown();
            }
        });

        socksStore.addReachMaxSizeCallback(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = new File("range_ips_out.txt");
                    if (f.exists()) {
                        f.delete();
                    }
                    f.createNewFile();

                    socksStore.record(f);
                } catch (Exception ex) {
                    LOGGER.error("Failed while recording output to simple_ips_out.txt ", ex);
                }

            }
        });

        socksStore.setMaxSize(nb);

        LOGGER.info("Start scanning " + nb + " hosts for Socks5 proxy at: " + startTime);
        while (ripsCf.hasNext()) {
            SocksProxyAddress spa = ripsCf.next();
            LOGGER.info("Connecting to: " + spa);
            tcpSocksScanner.scanSPA(spa);
        }
    }

    public void scanSocks5SingleIps() {
        SPAStore socksStore = new SPAStore();
        TCPSocksScanner tcpSocksScanner = new TCPSocksScanner(10, socksStore);
        SingleIpsConfig sipsCf = new SingleIpsConfig(true);
        int nb = sipsCf.getFinalCount();

        final long startTime = System.currentTimeMillis();
        socksStore.addReachMaxSizeCallback(new Runnable() {
            @Override
            public void run() {
                long tmp = System.currentTimeMillis();
                LOGGER.info("Total scanning time: " + (tmp - startTime));

                tcpSocksScanner.shutdown();
            }
        });

        socksStore.addReachMaxSizeCallback(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = new File("simple_ips_out.txt");
                    if (f.exists()) {
                        f.delete();
                    }
                    f.createNewFile();

                    socksStore.record(f);
                } catch (Exception ex) {
                    LOGGER.error("Failed while recording output to simple_ips_out.txt ", ex);
                }

            }
        });

        socksStore.setMaxSize(nb);

        LOGGER.info("Start scanning " + nb + " hosts for Socks5 proxy at: " + startTime);
        while (sipsCf.hasNext()) {
            SocksProxyAddress spa = sipsCf.next();
            LOGGER.info("Connecting to: " + spa);
            tcpSocksScanner.scanSPA(spa);
        }
    }

    public static void main(String[] args) throws Exception {
        MainScanner ms = new MainScanner();
        //ms.scanSocks5RangeIps();
        ms.scanSocks5SingleIps();

    }
}
