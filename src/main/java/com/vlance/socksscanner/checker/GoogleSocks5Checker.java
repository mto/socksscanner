package com.vlance.socksscanner.checker;

import com.vlance.socksscanner.spa.SPAStore;
import com.vlance.socksscanner.spa.SocksProxyAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.proxy.Socks5ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date: 10/10/17
 */
public class GoogleSocks5Checker {

    private final static Logger LOGGER = LoggerFactory.getLogger(GoogleSocks5Checker.class);

    private final String TARGET_HOST = "google.com";

    private final String TARGET_NAME = "Google";

    private final ExecutorService executor;

    private final ChannelFactory<NioSocketChannel> cfactory;

    private final SPAStore spaStore;

    public GoogleSocks5Checker(int nbThreads) {
        executor = Executors.newFixedThreadPool(nbThreads);
        cfactory = new ReflectiveChannelFactory<>(NioSocketChannel.class);

        spaStore = new SPAStore();
    }

    public void checkSocks5Proxy(SocksProxyAddress spa) {
        NioEventLoopGroup group = new NioEventLoopGroup(0, executor);

        Bootstrap bs = new Bootstrap();
        bs.group(group)
                .channelFactory(cfactory)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pp = ch.pipeline();
                        pp.addLast(new Socks5ProxyHandler(new InetSocketAddress(spa.host, spa.port)));

                        pp.addLast(new HttpClientCodec());
                        pp.addLast(new HttpObjectAggregator(1048576));
                        pp.addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                try {
                                    FullHttpResponse httpRes = (FullHttpResponse) msg;
                                    int httpStatus = httpRes.status().code();
                                    if (httpStatus == 200) {
                                        LOGGER.info("Proxy " + spa + " passes " + TARGET_NAME + " check");
                                        spaStore.add(spa);
                                    } else {
                                        LOGGER.info("Proxy " + spa + " does not pass " + TARGET_NAME + " check with HTTP status code: " + httpStatus);
                                    }
                                } finally {
                                    ctx.channel().close();
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                LOGGER.warn("Cannot connect from " + spa + " to " + TARGET_HOST);
                                ctx.channel().close();
                            }
                        });
                    }
                });

        ChannelFuture cf = bs.connect(new InetSocketAddress(TARGET_HOST, 80));
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    cf.channel().writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/?gfe_rd=cr&dcr=0&ei=5qjdWf3yPI2l8wfqppOoCg", false));
                    //group.shutdownGracefully();
                } else {
                    LOGGER.warn("Proxy " + spa + " does not support connection to " + TARGET_NAME);
                    group.shutdownGracefully();
                }
            }
        });

    }

    public void shutdown() {
        executor.shutdown();
    }

    public static void main(String[] args) {
        GoogleSocks5Checker gsc = new GoogleSocks5Checker(10);
        gsc.checkSocks5Proxy(new SocksProxyAddress("60.205.227.57", 1080));

        gsc.shutdown();
    }

}
