package com.vlance.socksscanner;

import com.vlance.socksscanner.handler.UDPSocks5Recognizer;
import com.vlance.socksscanner.spa.SPAStore;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.socksx.v5.*;

import java.net.InetSocketAddress;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/10/17
 */
public class UDPSocks5Scanner {

    private final Bootstrap bs;

    private final SPAStore spaStore = new SPAStore();

    public UDPSocks5Scanner(int nbThreads) {
        EventLoopGroup workers = new NioEventLoopGroup(nbThreads);

        bs = new Bootstrap()
                .group(workers)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pp = ch.pipeline();

                        pp.addLast(new Socks5ClientEncoder(Socks5AddressEncoder.DEFAULT));

                        pp.addLast(new UDPSocks5Recognizer(ch, spaStore));
                    }
                });
    }

    public void scanAddress(String host, int port) {
        ChannelFuture cf = bs.connect(new InetSocketAddress(host, port));
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    f.channel().writeAndFlush(new DefaultSocks5CommandRequest(Socks5CommandType.UDP_ASSOCIATE,
                            Socks5AddressType.IPv4, host, port));
                } else {
                    cf.channel().close();
                }
            }
        });
    }

}
