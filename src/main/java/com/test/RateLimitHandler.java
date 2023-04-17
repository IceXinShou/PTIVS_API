package com.test;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class RateLimitHandler extends ChannelDuplexHandler {
    private static final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>(); // 速率限制紀錄 (ip, 計數器)

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof HttpRequest) { // 如果型別為 HttpRequest
            String ip = ((HttpRequest) msg).headers().get("CF-Connecting-IP");

            if (!rateLimiters.computeIfAbsent(ip, k -> RateLimiter.create(20.0 / 60.0)).tryAcquire()) { // 當速度不被允許
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS))
                        .addListener(ChannelFutureListener.CLOSE);
                return;
            }
        }

        ctx.fireChannelRead(msg); // 傳遞至下一個處理器
    }
}
