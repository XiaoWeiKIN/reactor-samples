package wangxw.tcp.echo;

import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

/**
 * @Author: wangxw
 * @DateTime: 2021/9/12
 * @Description: TODO
 */
public class EchoServer {

    public static void main(String[] args) {
        DisposableServer server = TcpServer.create()
                .host("localhost")
                .port(8080)
                .bindNow();

        server.onDispose()
                .block();
    }
}
