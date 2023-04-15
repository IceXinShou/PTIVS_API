package com.test;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.test.HTML_Analyze.*;
import static com.test.PageKey.*;

public class Main {
    private static final String ALLOWED_HOST = "api.xserver.tw";
    public static final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    public static final Map<String, JSONObject> profileDatas = new HashMap<>();
    public static String defaultID = null;
    public static String defaultPWD = null;
    private final int port;
    public final SslContext sslCtx;

    public Main(String[] args) throws SSLException {
        this.port = Integer.parseInt(args[0]);
        Main.defaultID = args[1];
        Main.defaultPWD = args[2];

        this.sslCtx = SslContextBuilder.forServer(new File(args[3]), new File(args[4])).build();
    }

    public static void main(String[] args) throws Exception {
        new Main(args).run();
    }


    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NotNull SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();

                            // Add SSL handler
                            p.addLast(sslCtx.newHandler(ch.alloc()));

                            // Add HTTP codec
                            p.addLast(new HttpServerCodec());
                            p.addLast(new DomainLimit());

                            // RateLimit
                            p.addLast(new RateLimitHandler());


                            // Add object aggregator
                            p.addLast(new HttpObjectAggregator(65536));

                            // Add business logic handler
                            p.addLast(new HttpClientHandler());
                        }
                    });
            ChannelFuture f = b.bind(port).sync();

            System.out.println("Server started: \nlocalhost:" + port + '\n');

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.print(getTime() + " Connected: " + ctx.channel().remoteAddress() + " -> ");
            super.channelActive(ctx);
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            String uri = request.uri();
            HttpMethod method = request.method();
            HttpHeaders headers = request.headers();
            String realIP = headers.get("CF-Connecting-IP");
            System.out.println(headers.get("Host"));
            System.out.println(getTime() + ' ' + method + ": " + uri + " (" + realIP + ")\n");

            String[] args = uri.split("/");
            if (args.length < 3 || uri.equals("/favicon.ico") || !args[1].equals("ptivs") || method != HttpMethod.GET) {
                notFound(ctx);
                return;
            }

            String cookieString = headers.get(HttpHeaderNames.COOKIE);
            Map<String, List<String>> parameters = new QueryStringDecoder(uri).parameters();
            ResponseManager response = new ResponseManager(ctx);
            AuthManager authManager;
            LoginManager login;
            try {
                authManager = new AuthManager(new CookiesManager(cookieString), parameters, realIP);
                login = authManager.loginManager;
            } catch (ErrorException e) {
                response.errors.add(e.getMessage());
                response.responseJSON.put("time", LocalDateTime.now().toString());
                ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if (authManager.cookie != null) {
                response.cookies.add(authManager.cookie);
            }

            switch (args[2]) {
                case "absent": {
                    // 學期缺曠課 010010
                    putData(readAbsent(login.fetchPageData(ABSENT)), response);
                    break;
                }

                case "history_absent": {
                    // 歷年缺曠課 010030
                    putData(readHistoryAbsent(login.fetchPageData(HISTORY_ABSENT)), response);
                    break;
                }

                case "rewards": {
                    // 學期獎懲 010040
                    putData(readRewards(login.fetchPageData(REWARDS)), response);
                    break;
                }

                case "score": {
                    // 學期成績 010090
                    putData(readScore(login.fetchPageData(SCORE)), response);
                    break;
                }

                case "history_rewards": {
                    // 歷年獎懲 010050
                    putData(readHistoryRewards(login.fetchPageData(HISTORY_REWARDS)), response);
                    break;
                }

                case "punished_cancel_log": {
                    // 銷過紀錄 010060
                    putData(readPunishedCancelLog(login.fetchPageData(PUNISHED_CANCEL_LOG)), response);
                    break;
                }

                case "clubs": {
                    // 參與社團 010070
                    putData(readClubs(login.fetchPageData(CLUBS)), response);
                    break;
                }

                case "cadres": {
                    // 擔任幹部 010080
                    putData(readCadres(login.fetchPageData(CADRES)), response);
                    break;
                }

                case "history_score": {
                    // 歷年成績 010110
                    putData(readHistoryScore(login.fetchPageData(HISTORY_SCORE)), response);
                    break;
                }

                case "class_table": {
                    // 課表 010130
                    putData(readClassTable(login.fetchPageData(CLASS_TABLE)), response);
                    break;
                }

                default: {
                    notFound(ctx);
                    return;
                }
            }

            if (response.responseJSON.has("data")) {
                response.responseJSON.getJSONObject("data").put("profile", authManager.profile);
            }

            response.responseJSON.put("time", LocalDateTime.now().toString());
            ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
        }

        private void putData(JSONObject data, final ResponseManager api) {
            if (data != null) {
                api.responseJSON.put("data", data);
            } else {
                api.errors.add("cannot get data");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        private void notFound(final ChannelHandlerContext ctx) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getTime() {
        return "[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "]";
    }
}