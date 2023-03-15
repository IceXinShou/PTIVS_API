package com.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.json.JSONObject;

import java.time.LocalDateTime;

import static com.test.HTML_Analyze.*;

public class Main {

    private final int port;

    public Main(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = 80;
        new Main(port).run();
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new HttpServerInitializer());

            Channel ch = b.bind(port).sync().channel();
            System.out.println("Server started: \nlocalhost:" + port + '\n');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("codec", new HttpServerCodec());
            pipeline.addLast("aggregator", new HttpObjectAggregator(512 * 1024));
            pipeline.addLast("handler", new HttpServerHandler());
        }
    }

    private static class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Client connected from " + ctx.channel().remoteAddress() + " at " + LocalDateTime.now());
            super.channelActive(ctx);
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            LoginManager login = new LoginManager();
            API_Response api = new API_Response(ctx);
            String uri = request.uri();
            HttpMethod method = request.method();

            String responseContent = null;

            String[] args = uri.split("/");
            if (args.length < 3 || uri.equals("/favicon.ico") || !args[1].equals("ptivs") || method != HttpMethod.GET) {
                notFound(ctx);
                return;
            }

            System.out.println(method + ": " + uri);

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            String id = queryStringDecoder.parameters().get("name").get(0);
            String pwd = queryStringDecoder.parameters().get("pwd").get(0);
            login.onLogin(api, id, pwd);

            switch (args[2]) {
                case "absent": {
                    // 學期缺曠課 010010
                    responseContent = login.getPageData("010010");
                    break;
                }

                case "history_absent": {
                    // 歷年缺曠課 010030
                    responseContent = login.getPageData("010030");
                    break;
                }

                case "rewards": {
                    // 學期獎懲 010040
                    responseContent = login.getPageData("010040");
                    break;
                }

                case "history_rewards": {
                    // 歷年獎懲 010050
                    JSONObject data = getHistoryRewards(login.getPageData("010050"), id);
                    if (data != null) {
                        api.responseJSON.put("data", data);
                    } else {
                        api.errors.add("cannot get data");
                    }

                    break;
                }

                case "punished_log": {
                    // 銷過紀錄 010060
                    responseContent = login.getPageData("010060");
                    break;
                }


                case "clubs": {
                    // 參與社團 010070
                    JSONObject data = getClubs(login.getPageData("010070"), id);
                    if (data != null) {
                        api.responseJSON.put("data", data);
                    } else {
                        api.errors.add("cannot get data");
                    }

                    break;
                }

                case "cadres": {
                    // 擔任幹部 010080
                    JSONObject data = getCadres(login.getPageData("010080"), id);
                    if (data != null) {
                        api.responseJSON.put("data", data);
                    } else {
                        api.errors.add("cannot get data");
                    }

                    break;
                }

                case "score": {
                    // 學期成績 010090
                    responseContent = login.getPageData("010090");
                    break;
                }

                case "history_score": {
                    // 歷年成績 010110
                    JSONObject data = getHistoryScore(login.getPageData("010110"), id);
                    if (data != null) {
                        api.responseJSON.put("data", data);
                    } else {
                        api.errors.add("cannot get data");
                    }
                    break;
                }

                case "class_table": {
                    // 課表 010130
                    JSONObject data = getClassTable(login.getPageData("010130"), id);
                    if (data != null) {
                        api.responseJSON.put("data", data);
                    } else {
                        api.errors.add("cannot get data");
                    }

                    break;
                }

                default: {
                    notFound(ctx);
                    return;
                }
            }

            if (responseContent != null)
                System.out.println(responseContent);

            ctx.writeAndFlush(api.getResponse()).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        private void notFound(ChannelHandlerContext ctx) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}