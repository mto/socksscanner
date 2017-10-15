package com.vlance.socksscanner.spa;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/10/17
 */
public class SocksProxyAddress {

    public final String host;

    public final int port;

    public SocksProxyAddress(String _host, int _port) {
        host = _host;
        port = _port;
    }

    @Override
    public String toString() {
        return "Socks_" + " " + host + ":" + port;
    }
}
