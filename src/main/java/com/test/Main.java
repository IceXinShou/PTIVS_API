package com.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.test.HTML_Analyze.*;

public class Main {

    private final int port;
    private static final Map<String, JSONObject> profileData = new HashMap<>();

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
            String uri = request.uri();
            HttpMethod method = request.method();
            System.out.println(method + ": " + uri);

            LoginManager login = new LoginManager();
            API_Response api = new API_Response(ctx);

            String[] args = uri.split("/");
            if (args.length < 3 || uri.equals("/favicon.ico") || !args[1].equals("ptivs") || method != HttpMethod.GET) {
                notFound(ctx);
                return;
            }


            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            String id = "", pwd = "";
            try {
                id = queryStringDecoder.parameters().get("name").get(0);
                pwd = queryStringDecoder.parameters().get("pwd").get(0);
            } catch (Exception e) {
                api.errors.add("cannot get parameters: 'name' or 'pwd'");
            }

            if (api.errors.isEmpty()) {
                login.onLogin(api, id, pwd);

                switch (args[2]) {
                    case "absent": {
                        // 學期缺曠課 010010
//                        responseContent = login.getPageData("010010");
                        break;
                    }

                    case "history_absent": {
                        // 歷年缺曠課 010030
                        JSONObject data = getHistoryAbsent(login.getPageData("010030"));
                        if (data != null) {
                            api.responseJSON.put("data", data);
                        } else {
                            api.errors.add("cannot get data");
                        }

                        break;
                    }

                    case "rewards": {
                        // 學期獎懲 010040
//                        responseContent = login.getPageData("010040");
                        break;
                    }

                    case "history_rewards": {
                        // 歷年獎懲 010050

                        JSONObject data = getHistoryRewards(login.getPageData("010050"));
                        if (data != null) {
                            api.responseJSON.put("data", data);
                        } else {
                            api.errors.add("cannot get data");
                        }

                        break;
                    }

                    case "punished_cancel_log": {
                        // 銷過紀錄 010060
                        JSONObject data = getPunishedCancelLog(login.getPageData("010060"));
                        if (data != null) {
                            api.responseJSON.put("data", data);
                        } else {
                            api.errors.add("cannot get data");
                        }

                        break;
                    }


                    case "clubs": {
                        // 參與社團 010070
                        JSONObject data = getClubs(login.getPageData("010070"));
                        if (data != null) {
                            api.responseJSON.put("data", data);
                        } else {
                            api.errors.add("cannot get data");
                        }

                        break;
                    }

                    case "cadres": {
                        // 擔任幹部 010080
                        JSONObject data = getCadres(login.getPageData("010080"));
                        if (data != null) {
                            api.responseJSON.put("data", data);
                        } else {
                            api.errors.add("cannot get data");
                        }

                        break;
                    }

                    case "score": {
                        // 學期成績 010090
//                        responseContent = login.getPageData("010090");
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
                        JSONObject data = getClassTable(login.getPageData("010130"));
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
            }

            if (api.responseJSON.has("data")) {
                api.responseJSON.getJSONObject("data").put("profile", profileData.getOrDefault(id, getProfile(login)));
            }
            api.responseJSON.put("time", LocalDateTime.now().toString());
            ctx.writeAndFlush(api.getResponse()).addListener(ChannelFutureListener.CLOSE);
        }

        private JSONObject getProfile(final LoginManager login) {
            Elements userDatas = Jsoup.parse(login.getPageData("010070")).getElementsByTag("table").get(0).getElementsByTag("tr");
            JSONObject output = new JSONObject();
            Elements userData = userDatas.last().children();
            String semesterStr = userData.get(0).text().trim();
            output.put("name", userDatas.first().child(0).text().trim().split(" ： ")[1]);
            output.put("semester", Integer.parseInt(semesterStr.substring(0, semesterStr.lastIndexOf("學年"))));
            output.put("semester2", semesterStr.endsWith("第一學期") ? 1 : 2);
            output.put("class", userData.get(1).text().trim());
            output.put("id", userData.get(2).text().trim().split("：")[1]);
            return output;
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        private void notFound(final ChannelHandlerContext ctx) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}