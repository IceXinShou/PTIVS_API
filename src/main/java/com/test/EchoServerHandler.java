package com.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;

        // 讀取收到的訊息並轉換為字串
        String received = in.toString(CharsetUtil.UTF_8);
        // 如果收到 Hello 訊息，則回傳 World
        if ("Hello".equals(received)) {
            ctx.write(Unpooled.copiedBuffer("World", CharsetUtil.UTF_8));
            ctx.flush();
            System.out.println("Message received: " + received);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected from " + ctx.channel().remoteAddress() + " at " + LocalDateTime.now());
        super.channelActive(ctx);
    }
}