package com.atguigu.gmall.item.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情微服务提供的feign接口
 */

/**
 * @FeignClient 是Spring Cloud中用于创建声明式的Web服务客户端的注解。这个注解允许你定义一个接口，该接口可以用于调用远程服务。
 * @FeignClient注解的属性name和path用于配置客户端的基本信息。
 * @FeignClient：这个注解表明这个接口是一个Feign客户端，用于与远程服务进行通信。Feign客户端是基于HTTP的，它提供了一种简化的方式来定义服务调用接口。
 *
 * name = "service-product"：这里的name属性指定了远程服务的名称。这个名称通常与服务注册中心（如Eureka）中服务的名称相对应。
 * 当你在服务注册中心中注册了一个服务，你可以给它一个名称，例如service-product。
 * 这样，当你的Feign客户端需要调用这个服务时，它就会查找注册中心中名为service-product的服务实例，并与这些实例进行通信。
 *
 * path = "/item/info"：这里的path属性指定了服务调用的基础路径。当你定义Feign客户端接口中的方法时，你不需要在方法签名中包含完整的URL路径，只需要指定相对路径。
 * Feign会自动将这些相对路径与path属性中的基础路径结合起来，形成完整的请求URL。
 * 例如，如果你定义了一个方法@GetMapping("/detail")，Feign会将请求发送到http://service-item/item/info/detail（假设service-item是服务的名称）。
 *
 * contextId 属性用于指定 Feign 客户端的上下文 ID。在 Spring 中，每个 HTTP 请求都与一个特定的请求上下文关联，
 * 这个上下文包含了请求的相关信息，如请求头、请求参数等。通过指定 contextId，你可以为不同的 Feign 客户端创建不同的请求上下文，
 * 这对于处理需要不同配置的多个 Feign 客户端时非常有用。例如，你可能需要为不同的服务设置不同的超时时间、错误处理策略等，这时就可以通过 contextId 来区分。
 */
@FeignClient(name = "service-item", path = "/item/info", contextId = "itemFeign")
public interface ItemFeign {
    /**
     * 获取商品详情页需要的全部数据
     * @param skuId
     * @return
     */
    @GetMapping("/getItemInfo/{skuId}")
    public Map<String, Object> getItemInfo(@PathVariable Long skuId);
}
