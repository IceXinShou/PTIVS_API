package tw.xserver.manager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketManager {
    private static WebSocketManager instance;
    private ChannelHandlerContext channelHandlerContext = null;

    private WebSocketManager() {
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void setChannelHandlerContext(ChannelHandlerContext ctx) {
        this.channelHandlerContext = ctx;
    }

    public void send(String message) {
        if (channelHandlerContext != null && channelHandlerContext.channel().isActive()) {
            TextWebSocketFrame frame = new TextWebSocketFrame(message);
            channelHandlerContext.writeAndFlush(frame);
        }
    }
}
