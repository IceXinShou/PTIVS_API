package com.test;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

import static com.test.Main.rateLimiters;

public class ApiRateLimitHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String ip = getIpAddress(ctx, request);
            if (rateLimiters.computeIfAbsent(ip, k -> RateLimiter.create(30.0 / 60.0)).tryAcquire()) {
                ctx.fireChannelRead(msg);
            } else {
                sendTooManyPackageError(ctx);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private String getIpAddress(ChannelHandlerContext ctx, HttpRequest request) {
        String ipAddress = request.headers().get("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            ipAddress = remoteAddress.getAddress().getHostAddress();
        }
        return ipAddress;
    }

    private void sendTooManyPackageError(ChannelHandlerContext ctx) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS);
        response.headers().set("Content-Type", "text/plain; charset=UTF-8");
        response.content().writeBytes("Too many requests".getBytes());
        ctx.writeAndFlush(response);
        ctx.close();
    }
}
