package tw.xserver.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import tw.xserver.manager.JSONResponseManager;
import tw.xserver.util.ErrorException;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tw.xserver.Main.favicon;
import static tw.xserver.Util.getTime;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        /* 新連線 */
        System.out.println(getTime() + " Connected: " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object obj) {
        // 如果連線類型不是「請求」，回傳「錯誤」
        if (!(obj instanceof FullHttpRequest request)) {
            sendError(ctx, "unknown error", HttpResponseStatus.BAD_REQUEST);
            return;
        }

        HttpMethod method = request.method();
        HttpHeaders headers = request.headers();
        String uri = request.uri();
        String[] args = uri.split("/");

        /* WebSocket Connection */
        if ("Upgrade".equalsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION)) &&
                "WebSocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE))) {
            ctx.pipeline().replace(this, "WebSocketHandler", new WebSocketHandler(ctx, request));
            return;
        }

        /* 過濾請求 */
        if (args.length == 2 && args[1].equals("test")) {
            // 回傳測試內容
            sendTest(ctx);
            return;
        }

        if (uri.equals("/favicon.ico")) {
            // 請求縮圖
            sendFavicon(ctx);
            return;
        }

        if (args.length < 2 || !args[1].equals("ptivs")) {
            // 過濾不當請求
            sendError(ctx, "unsupported uri", HttpResponseStatus.NOT_FOUND);
            return;
        }

        /* 初始化參數 */
        String realIP = headers.get("CF-Connecting-IP");
        if (realIP == null)
            realIP = headers.get("Host").split(":")[0];

        System.out.println(getTime() + ' ' + method + ": " + uri + " (" + realIP + ")");

        /* 分工 */
        try {
            if (method.equals(HttpMethod.POST)) {
                new PostHandler(ctx, request); // POST 請求
            } else if (method.equals(HttpMethod.GET)) {
                new GetHandler(ctx, request); // GET 請求
            } else {
                sendError(ctx, "Unsupported request method: " + method.name(), HttpResponseStatus.METHOD_NOT_ALLOWED);
                return;
            }
        } catch (ErrorException e) {
            sendError(ctx, e.getMessage(), e.status);
            return;
        } catch (IOException e) {
            sendError(ctx, e.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        if (ctx.channel().isOpen()) {
            sendError(ctx, "cannot found", HttpResponseStatus.BAD_REQUEST);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void sendTest(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer("OK", UTF_8)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public static void sendError(ChannelHandlerContext ctx, String message, HttpResponseStatus status) {
        JSONResponseManager response = new JSONResponseManager(ctx);
        response.status = status;
        response.error = message;
        ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendFavicon(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(favicon));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/x-icon");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, favicon.length);
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=31557600");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}

