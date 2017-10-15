package com.vlance.socksscanner.handler;

import com.vlance.socksscanner.spa.SPAStore;
import com.vlance.socksscanner.spa.SocksProxyAddress;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v4.Socks4CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/15/17
 */
public class TCPSocks4Recognizer extends ChannelInboundHandlerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(TCPSocks4Recognizer.class);

    private final SPAStore spaStore;

    private final SocketChannel sc;

    public TCPSocks4Recognizer(SocketChannel _sc, SPAStore _spaStore) {
        sc = _sc;
        spaStore = _spaStore;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress rmAddr = sc.remoteAddress();
        if (msg instanceof Socks4CommandResponse) {
            try {
                Socks4CommandResponse s4res = (Socks4CommandResponse) msg;

                if (s4res.status().isSuccess()) {
                    LOGGER.info("Public Socks4 proxy TCP is available on: " + rmAddr.getHostString() + ":" + rmAddr.getPort());

                    spaStore.add(new SocksProxyAddress(rmAddr.getHostString(), rmAddr.getPort()));
                } else {
                    LOGGER.info("Failed to connect to Socks4 TCP proxy on: " + rmAddr.getHostString() + ":" + rmAddr.getPort());
                    spaStore.addFailedSpa(new SocksProxyAddress(rmAddr.getHostString(), rmAddr.getPort()));
                }
            } finally {
                LOGGER.debug("Closing channel " + rmAddr.getHostString() + ":" + rmAddr.getPort());
                ctx.channel().close();
            }
        } else {
            LOGGER.info("Service other than Socks4 is available on: " + rmAddr.getHostString() + ":" + rmAddr.getPort());
            spaStore.addFailedSpa(new SocksProxyAddress(rmAddr.getHostString(), rmAddr.getPort()));
            ctx.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        InetSocketAddress rmAddr = sc.remoteAddress();
        LOGGER.warn("Error while handshaking to: " + rmAddr.getHostString() + ":" + rmAddr.getPort());
        spaStore.addFailedSpa(new SocksProxyAddress(rmAddr.getHostString(), rmAddr.getPort()));

        ctx.channel().close();
    }
}
