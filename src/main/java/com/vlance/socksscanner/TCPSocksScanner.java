package com.vlance.socksscanner;

import com.vlance.socksscanner.handler.TCPNoAuthSocks5Recognizer;
import com.vlance.socksscanner.handler.TCPSocks4Recognizer;
import com.vlance.socksscanner.spa.SPAStore;
import com.vlance.socksscanner.spa.SocksProxyAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4ClientDecoder;
import io.netty.handler.codec.socksx.v4.Socks4ClientEncoder;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/10/17
 */
public class TCPSocksScanner {

    private final static Logger LOGGER = LoggerFactory.getLogger(TCPSocksScanner.class);

    private final Bootstrap bs;

    private final EventLoopGroup workers;

    private final SPAStore spaStore;

    public TCPSocksScanner(int nbThreads, SPAStore _tcpSpaStore) {
        spaStore = _tcpSpaStore;

        workers = new NioEventLoopGroup(nbThreads);

        bs = new Bootstrap()
                .group(workers)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pp = ch.pipeline();

                        pp.addLast("socks5_encoder", new Socks5ClientEncoder(Socks5AddressEncoder.DEFAULT));
                        pp.addLast("socks4_encoder", Socks4ClientEncoder.INSTANCE);
                        pp.addLast("socks5_decoder", new Socks5InitialResponseDecoder());
                        pp.addLast("socks4_decoder", new Socks4ClientDecoder());

                        pp.addLast(new TCPNoAuthSocks5Recognizer(ch, spaStore));
                        pp.addLast(new TCPSocks4Recognizer(ch, spaStore));
                    }
                });
    }

    public void scanSPA(SocksProxyAddress spa){
        scanAddress(spa.host, spa.port);
    }

    public void scanAddress(String host, int port) {
        ChannelFuture cf = bs.connect(new InetSocketAddress(host, port));
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    f.channel().writeAndFlush(new DefaultSocks4CommandRequest(Socks4CommandType.CONNECT, host, port));
                    f.channel().writeAndFlush(new DefaultSocks5InitialRequest(Socks5AuthMethod.NO_AUTH));
                } else {
                    LOGGER.warn("Cannot connect to Socks5 proxy on: " + host + ":" + port);
                    spaStore.addFailedSpa(new SocksProxyAddress(host, port));
                    cf.channel().close();
                }
            }
        });
    }

    public void shutdown(){
        workers.shutdownGracefully();
    }

}
