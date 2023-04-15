package com.test;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.jetbrains.annotations.NotNull;

public class DomainLimit extends ChannelDuplexHandler {

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            if (!request.headers().get("Host").equals("api.xserver.tw")) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                response.headers().set("Content-Type", "text/plain; charset=UTF-8");
                response.content().writeBytes("BAD_REQUEST".getBytes());
                ctx.writeAndFlush(response);
                ctx.close();
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }
}
