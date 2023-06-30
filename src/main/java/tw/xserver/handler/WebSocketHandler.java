package tw.xserver.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.*;
import org.jetbrains.annotations.NotNull;
import tw.xserver.manager.WebSocketManager;

import static tw.xserver.Util.getTime;
import static tw.xserver.handler.ClientHandler.sendError;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    public WebSocketHandler(ChannelHandlerContext ctx, HttpRequest req) {
        if (!req.headers().get("Host").equals("127.0.0.1:443")) {
            sendError(ctx, "You have no permission", HttpResponseStatus.UNAUTHORIZED);
            return;
        }
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true);
        WebSocketServerHandshaker handshake = wsFactory.newHandshaker(req);
        if (handshake == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshake.handshake(ctx.channel(), req);
        }

        WebSocketManager.getInstance().setChannelHandlerContext(ctx);
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            System.out.println(getTime() + " Client Channel: " + ctx.channel());

            if (msg instanceof BinaryWebSocketFrame wsf) {
                System.out.println(getTime() + " BinaryWebSocketFrame Received: " + wsf.content());
            } else if (msg instanceof TextWebSocketFrame wsf) {
                System.out.println(getTime() + " TextWebSocketFrame Received: " + wsf.text());
                ctx.channel().writeAndFlush(new TextWebSocketFrame("Message received: " + wsf.text()));
            } else if (msg instanceof PingWebSocketFrame wsf) {
                System.out.println(getTime() + " PingWebSocketFrame Received: " + wsf.content());
            } else if (msg instanceof PongWebSocketFrame wsf) {
                System.out.println(getTime() + " PongWebSocketFrame Received: " + wsf.content());
            } else if (msg instanceof CloseWebSocketFrame wsf) {
                System.out.println(getTime() + " CloseWebSocketFrame Received: ");
                System.out.println(getTime() + " ReasonText: " + wsf.reasonText());
                System.out.println(getTime() + " StatusCode: " + wsf.statusCode());
            } else {
                System.out.println(getTime() + " Unsupported WebSocketFrame");
            }
        }
    }

    protected String getWebSocketURL(HttpRequest req) {
        String url = "ws://" + req.headers().get("Host") + req.uri();
        System.out.println(getTime() + " New WebSocket: " + url);
        return url;
    }
}
