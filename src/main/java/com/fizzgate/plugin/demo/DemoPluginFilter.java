package com.fizzgate.plugin.demo;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.fizzgate.config.ProxyWebClientConfig;
import com.fizzgate.plugin.FizzPluginFilter;
import com.fizzgate.plugin.FizzPluginFilterChain;
import com.fizzgate.spring.http.server.reactive.ext.FizzServerHttpRequestDecorator;
import com.fizzgate.util.NettyDataBufferUtils;

import javax.annotation.Resource;
import java.util.Map;

@Component(DemoPluginFilter.DEMO_PLUGIN) // 必须，且为插件 id
public class DemoPluginFilter implements FizzPluginFilter {

    public static final String DEMO_PLUGIN = "demoPlugin"; // 插件 id

    @Resource(name = ProxyWebClientConfig.proxyWebClient)
    private WebClient webClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, Map<String, Object> config) {

        // 当前面应用了 RequestBodyPlugin.REQUEST_BODY_PLUGIN 插件（可通过管理后台配置），或者当前插件继承自 RequestBodyPlugin.REQUEST_BODY_PLUGIN，才能这样强转
        // 如果不需要修改请求体，则无需强转；FizzServerHttpRequestDecorator 提供了 setBody 方法，用于修改请求体
        FizzServerHttpRequestDecorator request = (FizzServerHttpRequestDecorator) exchange.getRequest();

        return
                request.getBody().defaultIfEmpty(NettyDataBufferUtils.EMPTY_DATA_BUFFER)
                       .single()
                       .flatMap(
                                body -> {
                                    // String bodyStr = body.toString(StandardCharsets.UTF_8); // 请求体对应的字符串

                                    // 若需调整转发到后端接口的请求体，可像下面这样设置新的体和类型
                                    /*
                                    String newRequestBody = "client " + bodyStr;
                                    request.setBody(newRequestBody);
                                    String newRequestBodyType = MediaType.APPLICATION_ATOM_XML_VALUE;
                                    request.getHeaders().put(HttpHeaders.CONTENT_TYPE, Collections.singletonList(newRequestBodyType));
                                    */

                                    /* 直接响应客户端 abc 文本
                                    if (true) {
                                        ServerHttpResponse clientResp = exchange.getResponse();
                                        return clientResp.writeWith(Mono.just(clientResp.bufferFactory().wrap("abc".getBytes())));
                                    }
                                    */

                                    // Route route = WebUtils.getRoute(exchange); // 请求匹配的路由，下面改路由配置，相当于动态路由
                                    // route.method(HttpMethod.HEAD/*改方法*/).nextHttpHostPort("http://127.0.0.1:6666"/*反向代理类型的路由，改地址*/);
                                    // route.backendService("改目标服务");
                                    // route.backendPath("改目标路径");

                                    /* 调远程接口
                                    Mono<String> remoteRespBodyStrMono = webClient.post()
                                                                                  .uri("http://127.0.0.1:9094/@ypath")
                                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                                  .bodyValue("{\"i\":100}")
                                                                                  .exchange()
                                                                                  .flatMap(
                                                                                          remoteResp -> {
                                                                                              return remoteResp.bodyToMono(String.class) // 接口的响应体字符串
                                                                                                               .defaultIfEmpty(Constants.Symbol.EMPTY)
                                                                                                               .map(
                                                                                                                   remoteRespBodyStr -> {
                                                                                                                       return remoteRespBodyStr;
                                                                                                                   }
                                                                                                               );
                                                                                          }
                                                                                  );
                                    return
                                    remoteRespBodyStrMono.flatMap(
                                            s -> {
                                                System.err.println("http://127.0.0.1:9094/@ypath 响应体：" + s);
                                                return FizzPluginFilterChain.next(exchange); // 执行后续插件或其它逻辑
                                            }
                                    );
                                    */

                                    System.err.println("this is demo plugin"); // 本插件只输出这个
                                    return FizzPluginFilterChain.next(exchange); // 执行后续插件或其它逻辑

                                    /* 取到远程接口的响应，并修改，并响应客户端的例子
                                    ServerHttpResponse original = exchange.getResponse();
                                    FizzServerHttpResponseDecorator fizzServerHttpResponseDecorator = new FizzServerHttpResponseDecorator(original) {
                                        @Override
                                        public Publisher<? extends DataBuffer> writeWith(DataBuffer remoteResponseBody) {
                                            String str = remoteResponseBody.toString(StandardCharsets.UTF_8);
                                            HttpHeaders headers = getDelegate().getHeaders();
                                            headers.setContentType(MediaType.TEXT_PLAIN);
                                            headers.remove(HttpHeaders.CONTENT_LENGTH);
                                            NettyDataBuffer from = NettyDataBufferUtils.from("modified body: " + str);
                                            return Mono.just(from);
                                        }
                                    };
                                    ServerWebExchange build = exchange.mutate().response(fizzServerHttpResponseDecorator).build();
                                    return FizzPluginFilterChain.next(build); // 执行后续插件或其它逻辑
                                    */
                                }
                       );
    }
}
