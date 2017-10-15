package com.vlance.socksscanner.handler;

import com.vlance.socksscanner.spa.SPAStore;
import com.vlance.socksscanner.spa.SocksProxyAddress;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/10/17
 */
public class UDPSocks5Recognizer extends ChannelInboundHandlerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(UDPSocks5Recognizer.class);

    private final SPAStore spaStore;

    private final SocketChannel sc;

    public UDPSocks5Recognizer(SocketChannel _sc, SPAStore _spaStore) {
        sc = _sc;
        spaStore = _spaStore;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Socks5CommandResponse cmdRes = (Socks5CommandResponse) msg;
            if (Socks5CommandStatus.SUCCESS.equals(cmdRes.status())) {
                InetSocketAddress rmAddr = sc.remoteAddress();
                LOGGER.info("Socks5 proxy UDP is available on: " + rmAddr.getHostString() + ":" + rmAddr.getPort());

                spaStore.add(new SocksProxyAddress(rmAddr.getHostString(), rmAddr.getPort()));
            }
        } finally {
            ctx.channel().close();
        }
    }
}
