package com.shark.aio.data.monitorDeviceHj212.entity;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class HJ212Client {

    private static final String HOST = "127.0.0.1"; // 服务端地址
    private static final int PORT = 9999;          // 服务端端口

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new HJ212ClientInitializer());

            // 连接到服务端
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // 构造符合协议格式的消息
            String message = "##0722QN=20241130114316059;ST=32;CN=2051;PW=123456;MN=23923493201019;Flag=5;CP=&&DataTime=20241130113000;" +
                    "w01018-Cou=46.630,w01018-Avg=196.000,w01018-Min=196.000,w01018-Max=196.000,w01018-Flag=N;w21003-Cou=6.930,w21003-Avg=29.110,w21003-Min=29.110,w21003-Max=29.110,w21003-Flag=N;w21011-Cou=0.570,w21011-Avg=2.380,w21011-Min=2.380,w21011-Max=2.380,w21011-Flag=N;w21001-Cou=6.810,w21001-Avg=28.620,w21001-Min=28.620,w21001-Max=28.620,w21001-Flag=N;w01001-Avg=7.03,w01001-Min=7.00,w01001-Max=7.06,w01001-Flag=N;w00000-Cou=237.91,w00000-Avg=66.09,w00000-Min=392.51,w00000-Max=403.98,w00000-Avg=19796346.00,w00000-Min=19796286.00,w00000-Max=19796346.00,w00000-Flag=N;w01010-Avg=19.26,w01010-Min=19.20,w01010-Max=19.30,w01010-Flag=N&&0440\r\n";

            // 发送消息
            f.channel().writeAndFlush(message).sync();

            // 等待服务端的响应
            f.channel().closeFuture().sync();  // 等待连接关闭
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class HJ212ClientInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));  // 解码器
            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));  // 编码器
            pipeline.addLast(new HJ212ClientHandler());  // 处理响应的 Handler
        }
    }

    private static class HJ212ClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            // 处理服务端返回的消息
            System.out.println("收到服务端响应: " + msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
