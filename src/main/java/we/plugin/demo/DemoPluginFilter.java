package we.plugin.demo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import we.plugin.FizzPluginFilter;
import we.plugin.FizzPluginFilterChain;
import we.spring.http.server.reactive.ext.FizzServerHttpRequestDecorator;
import we.util.NettyDataBufferUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Component(DemoPluginFilter.DEMO_PLUGIN) // 必须，且为插件 id
public class DemoPluginFilter implements FizzPluginFilter {

    public static final String DEMO_PLUGIN = "demoPlugin"; // 插件 id

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, Map<String, Object> config) {

        FizzServerHttpRequestDecorator request = (FizzServerHttpRequestDecorator) exchange.getRequest();

        return
                request.getBody().defaultIfEmpty(NettyDataBufferUtils.EMPTY_DATA_BUFFER)
                        .single()
                        .flatMap(
                                body -> {
                                    // String bodyStr = body.toString(StandardCharsets.UTF_8); // 请求体对应的字符串
                                    // System.err.println("request body: " + bodyStr);

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

                                    System.err.println("this is demo plugin"); // 本插件只输出这个

                                    return FizzPluginFilterChain.next(exchange); // 执行后续插件或其它逻辑
                                }
                        );
    }
}
