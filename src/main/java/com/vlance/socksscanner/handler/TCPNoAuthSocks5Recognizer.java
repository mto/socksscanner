package com.vlance.socksscanner.handler;

import com.vlance.socksscanner.spa.SPAStore;
import com.vlance.socksscanner.spa.SocksProxyAddress;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/10/17
 */
public class TCPNoAuthSocks5Recognizer extends ChannelInboundHandlerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(TCPNoAuthSocks5Recognizer.class);

    private final SPAStore spaStore;

    private final SocketChannel sc;

    public TCPNoAuthSocks5Recognizer(SocketChannel _sc, SPAStore _spaStore) {
        sc = _sc;
        spaStore = _spaStore;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InetSocketAddress rmAddr = sc.remoteAddress();
        if (msg instanceof Socks5InitialResponse) {
            try {
                Socks5InitialResponse s5res = (Socks5InitialResponse) msg;

                if (Socks5AuthMethod.NO_AUTH.equals(s5res.authMethod())) {
                    LOGGER.info("Public Socks5 proxy TCP is available on: " + rmAddr.getHostString() + ":" + rmAddr.getPort());

                    spaStore.add(new SocksProxyAddress(rmAddr.getHostString(), rmAddr.getPort()));
                } else {
                    LOGGER.info("Only " + s5res.authMethod() + " Socks5 TCP is available on: " + rmAddr.getHostString() + ":" + rmAddr.getPort());
                    spaStore.addFailedSpa(new SocksProxyAddress(rmAddr.getHostString(), rmAddr.getPort()));
                }
            } finally {
                LOGGER.debug("Closing channel " + rmAddr.getHostString() + ":" + rmAddr.getPort());
                ctx.channel().close();
            }
        } else {
            LOGGER.info("Service other than Socks5 is available on: " + rmAddr.getHostString() + ":" + rmAddr.getPort());
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
