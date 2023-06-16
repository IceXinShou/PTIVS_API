package com.test.handler;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitHandler extends ChannelDuplexHandler {
    private static final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>(); // 速率限制紀錄 (ip, 計數器)
    private static final Set<String> allowList = new HashSet<>();

    public RateLimitHandler(String allowIP) {
        allowList.add(allowIP);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof HttpRequest) { // 如果型別為 HttpRequest
            String ip = ((HttpRequest) msg).headers().get("CF-Connecting-IP");
            if (ip != null && !allowList.contains(ip) && !rateLimiters.computeIfAbsent(ip, k -> RateLimiter.create(180.0 / 60.0)).tryAcquire()) { // 當速度不被允許
                System.out.println("<Rate Limited>");
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS))
                        .addListener(ChannelFutureListener.CLOSE);
                return;
            }
        }

        ctx.fireChannelRead(msg); // 傳遞至下一個處理器
    }
}
