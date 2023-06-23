package tw.xserver.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.*;
import org.jetbrains.annotations.NotNull;

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
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            System.out.println("Client Channel: " + ctx.channel());
            if (msg instanceof BinaryWebSocketFrame) {
                System.out.println("BinaryWebSocketFrame Received: ");
                System.out.println(((BinaryWebSocketFrame) msg).content());
            } else if (msg instanceof TextWebSocketFrame) {
                System.out.println("TextWebSocketFrame Received: ");
                ctx.channel().writeAndFlush(
                        new TextWebSocketFrame("Message recieved: " + ((TextWebSocketFrame) msg).text()));
                System.out.println(((TextWebSocketFrame) msg).text());
            } else if (msg instanceof PingWebSocketFrame) {
                System.out.println("PingWebSocketFrame Received: ");
                System.out.println(((PingWebSocketFrame) msg).content());
            } else if (msg instanceof PongWebSocketFrame) {
                System.out.println("PongWebSocketFrame Received: ");
                System.out.println(((PongWebSocketFrame) msg).content());
            } else if (msg instanceof CloseWebSocketFrame) {
                System.out.println("CloseWebSocketFrame Received: ");
                System.out.println("ReasonText: " + ((CloseWebSocketFrame) msg).reasonText());
                System.out.println("StatusCode: " + ((CloseWebSocketFrame) msg).statusCode());
            } else {
                System.out.println("Unsupported WebSocketFrame");
            }
        }
    }

    protected String getWebSocketURL(HttpRequest req) {
        String url = "ws://" + req.headers().get("Host") + req.uri();
        System.out.println(getTime() + " New WebSocket: " + url);
        return url;
    }
}
