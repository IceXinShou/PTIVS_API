package com.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResponseManager {
    private final ChannelHandlerContext ctx;
    public final List<String> errors = new ArrayList<>();
    public final List<String> warnings = new ArrayList<>();
    public final JSONObject responseJSON = new JSONObject("{\"success\":true}");
    public final List<Cookie> cookies = new ArrayList<>();
    public HttpResponseStatus responseStatus = HttpResponseStatus.OK;

    public ResponseManager(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public FullHttpResponse getResponse() {

        if (!errors.isEmpty()) {
            responseJSON.put("success", false);
            responseJSON.put("errors", new JSONArray(errors));
        }

        if (!warnings.isEmpty()) {
            responseJSON.put("warnings", new JSONArray(warnings));
        }

        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(responseJSON.toString().getBytes(CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, buffer);
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes())
                .set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookies));
        return response;
    }
}