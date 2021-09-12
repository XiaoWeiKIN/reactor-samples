package netty.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: wangxw
 * @DateTime: 2021/9/12
 * @Description: TODO
 */
@Slf4j
public class EchoServer {
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private Channel channel;

    @Before
    public void start() {
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boos", false));
        workGroup = new NioEventLoopGroup(Math.min(Runtime.getRuntime().availableProcessors() + 1, 32), new DefaultThreadFactory("worker", true));
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ServerHandler());
                    }
                });
        // bind
        ChannelFuture channelFuture = serverBootstrap.bind(8081).syncUninterruptibly();

        channel = channelFuture.channel();
        channel.closeFuture().addListener((ChannelFutureListener) future -> {
            log.info("channel {} has close", channel);
        });
    }

    @Test
    public void test() {
    }

    @After
    public void shutdown() {
        if (channel != null) {
            channel.close();
        }
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Slf4j
    @ChannelHandler.Sharable
    static class ServerHandler extends SimpleChannelInboundHandler<String> {

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            log.info("channelRegistered");
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            log.info("recvive msg : {}", msg);
        }
    }
}
