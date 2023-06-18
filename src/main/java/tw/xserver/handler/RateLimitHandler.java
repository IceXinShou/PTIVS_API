package tw.xserver.handler;

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

@SuppressWarnings("all")
public class RateLimitHandler extends ChannelDuplexHandler {
    // TODO: Unstable API Usage - RateLimiter
    private static final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>(); // 速率限制紀錄 (ip, 計數器)

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (!(msg instanceof HttpRequest request)) { // 傳遞至下一個處理器
            ctx.fireChannelRead(msg);
            return;
        }

        String ip = request.headers().get("CF-Connecting-IP");
        if (ip != null && !rateLimiters.computeIfAbsent(ip, k -> RateLimiter.create(180.0 / 60.0)).tryAcquire()) { // 當速度不被允許
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS))
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }

        ctx.fireChannelRead(msg);
    }
}
