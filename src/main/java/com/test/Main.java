package com.test;

import com.test.handler.ClientHandler;
import com.test.handler.DomainLimitHandler;
import com.test.handler.RateLimitHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.net.ssl.SSLException;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static final ConcurrentHashMap<String, JSONObject> profileDatas = new ConcurrentHashMap<>();
    public static byte[] favicon;
    public static String defaultID;
    public static String defaultPWD;
    public final SslContext sslCtx;
    private final int port;

    public Main(String[] args) throws SSLException {
        this.port = Integer.parseInt(args[0]);
        defaultID = args[1];
        defaultPWD = args[2];
        this.sslCtx = SslContextBuilder
                .forServer(new File(args[3]), new File(args[4]))
                .sslProvider(SslProvider.JDK)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        favicon = loadFavicon(args[5]);
    }

    public static void main(String[] args) throws Exception {
        new Main(args).run();
    }

    public class SSLChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
        }
    }
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 創建一個線程池，用於處理連接請求
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 創建一個線程池，用於處理連接請求
        try {
            ServerBootstrap b = new ServerBootstrap(); // 創建對象，用於配置伺服器參數

            b.group(bossGroup, workerGroup) // 指定網路事件處理器線程池
                    .channel(NioServerSocketChannel.class) // 指定服務端使用的通訊協議
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 指定每個客戶端連接的處理器
                        @Override
                        protected void initChannel(@NotNull SocketChannel ch) { // 添加自定義的伺服器處理器

                            ch.pipeline()
                                    .addLast(new SSLChannelInitializer()) // 設定加解密器，確保所有資料安全
                                    .addLast(new HttpServerCodec()) // 設定編解碼器
                                    .addLast(new DomainLimitHandler()) // 設定連線路徑限制
                                    .addLast(new RateLimitHandler()) // 設定流量限制
                                    .addLast(new HttpObjectAggregator(65536)) // 整合輸出入資料
                                    .addLast(new ClientHandler()); // 處理資料
                        }
                    });
            ChannelFuture f = b.bind(port).sync(); // 綁定至端口

            System.out.println("Server started\n");

            f.channel().closeFuture().sync(); // 等待程式結束並關閉端口
        } finally {
            bossGroup.shutdownGracefully(); // 關閉線程池
            workerGroup.shutdownGracefully(); // 關閉線程池
        }
    }

    private byte[] loadFavicon(String path) {
        try (InputStream in = new FileInputStream(path)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}