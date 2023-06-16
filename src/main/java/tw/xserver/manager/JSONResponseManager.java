package tw.xserver.manager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JSONResponseManager {
    public final List<String> warnings = new ArrayList<>();
    public final JSONObject json = new JSONObject("{\"success\":true}");
    public final List<Cookie> cookies = new ArrayList<>();
    private final ChannelHandlerContext ctx;
    public String error = null;
    public HttpResponseStatus status = HttpResponseStatus.OK;

    public JSONResponseManager(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public FullHttpResponse getResponse() {

        if (error != null) {
            json.put("success", false);
            json.put("errors", error);
        }

        if (!warnings.isEmpty()) {
            json.put("warnings", new JSONArray(warnings));
        }

        json.put("time", LocalDateTime.now().toString());

        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(json.toString().getBytes(CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buffer);

        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes())
                .set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookies));
        return response;
    }
}