package tw.xserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.jetbrains.annotations.NotNull;
import tw.xserver.handler.ClientHandler;
import tw.xserver.handler.RateLimitHandler;
import tw.xserver.manager.CacheManager;
import tw.xserver.manager.CertificateManager;

import javax.net.ssl.SSLException;
import java.io.*;
import java.sql.SQLException;
import java.util.Base64;

import static tw.xserver.Util.getTime;

public class Main extends ChannelInboundHandlerAdapter {
    public static byte[] favicon;
    public static String defaultID;
    public static String defaultPWD;
    private static SslContext sslCtx;
    private static int port;
    public static CertificateManager certificate;
    public static CacheManager cache;

    public static void main(String[] args) throws Exception {
        /* 輸入參數 */
        new Main(args).run();
    }

    public Main(String[] args) throws SSLException, SQLException {
        /* 主程式開始 */
        /* 初始化參數 */
        port = Integer.parseInt(args[0]);
        defaultID = args[1];
        defaultPWD = new String(Base64.getDecoder().decode(args[2]));
        sslCtx = SslContextBuilder.forServer( // 載入證書
                new File("./key/certchain.pem"), new File("./key/privatekey.pem")
        ).build();
        favicon = loadFavicon("./icon/favicon-32x32.png");

        /* 初始驗證資料庫 */
        certificate = new CertificateManager();

        /* 初始快取資料庫 */
        cache = new CacheManager();
    }

    public void run() throws Exception {
        /* 準備伺服器 */
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 處理連接請求
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 處理讀寫請求
        try {
            ServerBootstrap b = new ServerBootstrap(); // 配置伺服器參數

            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 服務端通訊協議
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NotNull SocketChannel ch) {
                            /* 添加多個處理器 */
                            final ChannelPipeline pipeline = ch.pipeline();

                            if (!ch.remoteAddress().getAddress().getHostAddress().equals("127.0.0.1")) {
                                // 若請求不來自本地端
                                pipeline.addLast(sslCtx.newHandler(ch.alloc())); // 加解密器
                                pipeline.addLast(new HttpServerCodec()); // 編解碼器
                                pipeline.addLast(new RateLimitHandler()); // 流量限制
                            } else {
                                pipeline.addLast(new HttpServerCodec()); // 編解碼器
                            }

                            pipeline.addLast(new HttpObjectAggregator(65536));// 整合輸出入資料
                            pipeline.addLast(new ClientHandler()); // 主要處理資料
                        }
                    });

            /* 綁定連接埠 */
            ChannelFuture f = b.bind(port).sync();

            System.out.println(getTime() + " server started");

            f.channel().closeFuture().sync(); // 等待程式結束並關閉端口
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

        /* 主程式結束 */
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