package tw.xserver.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.jetbrains.annotations.NotNull;

public class DomainLimitHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof HttpRequest request) { // 如果型別為 HttpRequest
            if (request.headers().contains("Host") && !request.headers().get("Host").equals("api.xserver.tw")) { // 當連線路徑不屬於 api.xserver.tw
                System.out.println("<Domain Filtered>");
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST))
                        .addListener(ChannelFutureListener.CLOSE);
                return;
            }
        }

        ctx.fireChannelRead(msg); // 傳遞至下一個處理器
    }
}
