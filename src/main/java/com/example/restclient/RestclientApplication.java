package com.example.restclient;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 왜 내부클래스 작성 시 정적 클래스로 만들어야 빈으로 동작하는가
 *
 * oracle tutorial nested classes
 * 내부에 있는 모든 클래스는 중첩 클래스이며 non-static nested class 와 static nested class 로 나뉜다
 * 이 중 non-static nested class 를 다른 말로 inner class라 부르며 이는 내부 클래스라고 명칭한다.
 */
@SpringBootApplication
public class RestclientApplication {

    @Component
    static class StaticInnerClass {
        public StaticInnerClass() {
            System.out.println("StaticInnerClass");

            // Local Class
            class LocalClass {
                public LocalClass() {
                    System.out.println("LocalClass");
                }
            }

            // Anonymous Class
            var runnable = new Runnable() {
                @Override
                public void run() {
                    System.out.println("Runnable");
                }
            };
        }
    }

    @Component // 부모클래스가 빈으로 등록이 되어있다면 innerClass 역시도 빈으로 등록이 된다. 예) OuterClass
    class InnerClass {
        public InnerClass() {
            System.out.println("InnerClass");
        }
    }

    // 빈 어노테이션이 붙은 메소드에서 다른 빈을 주입받을때에는 @Autowired 생략가능
    // 단독 생성자가 있는 경우에 생성자를 통해서 빈을 주입받을 때 생략이 가능한 것처럼
    // 빈 어노테이션이 붙은 메소드에서도 동일하게 적용이 된다.
    @Bean
    ApplicationRunner init(ErApi api) {
        return args -> {
//            System.out.printf("Hello World");
//            https://open.er-api.com/v6/latest 환율정보 API
            RestTemplate rt = new RestTemplate();
//            String res = rt.getForObject("https://open.er-api.com/v6/latest", String.class);
//            System.out.println(res);
//            Map<String, Object> res = rt.getForObject("https://open.er-api.com/v6/latest", Map.class);
//            System.out.println(res);
            Map<String, Map<String, Double>> res = rt.getForObject("https://open.er-api.com/v6/latest", Map.class);
            System.out.println(res.get("rates").get("KRW"));
            WebClient client = WebClient.create("https://open.er-api.com");
            Map<String, Map<String, Double>> res2= client.get().uri("/v6/latest").retrieve().bodyToMono(Map.class).block();
            System.out.println(res2.get("rates").get("KRW"));

            HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                    .builder(WebClientAdapter.forClient(client))
                    .build();
            ErApi erApi = httpServiceProxyFactory.createClient(ErApi.class);
            Map<String, Map<String, Double>> res3 = erApi.getLatest();
            System.out.println(res3.get("rates").get("KRW"));

            Map<String, Map<String, Double>> res4 = api.getLatest();
            System.out.println(res4.get("rates").get("KRW"));
        };
    }

    @Bean
    ErApi erApi() {
        WebClient webClient = WebClient.create("https://open.er-api.com");
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(ErApi.class);
    }
    interface ErApi {
        @GetExchange("/v6/latest")
        Map getLatest();
//        Mono<Map<String, Map<String, Double>>> getLatest();
    }
    public static void main(String[] args) {
        ConfigurableApplicationContext ac = SpringApplication.run(RestclientApplication.class, args);
        System.out.println(ac.getBean(StaticInnerClass.class));
        System.out.println(ac.getBean(OuterClass.InnerClass2.class));

        new StaticInnerClass();
    }

}
