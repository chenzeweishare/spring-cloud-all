package com.czw.order.controller;

import com.czw.order.command.OrderServiceCommand;
import com.czw.order.entity.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @GetMapping("/user/{id}")
    public User findById(@PathVariable Long id) {
        return this.restTemplate.getForObject("http://microservice-provider-user/" + id, User.class);
    }

    @GetMapping("/user/getIpAndPort")
    public String getIpAndPort() {
        return this.restTemplate.getForObject("http://microservice-provider-user/getIpAndPort", String.class);
    }

    @GetMapping("/log-user-instance")
    public void logUserInstance() {
        ServiceInstance serviceInstance = this.loadBalancerClient.choose("microservice-provider-user");
        // 打印当前选择的是哪个节点
        OrderController.LOGGER.info("{}:{}:{}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort());
    }


    /**
     * 通过command方法熔断, 限流, 超时, 降级
     * Hystrix调用接口默认2s是超时,超时后走降级方法
     * 默认报错20次, 超过20次就走降级
     *
     * @param id
     * @return
     */
    @GetMapping("/user/command/{id}")
    public User getUserByCommand(@PathVariable Long id) {
        LOGGER.info("================Hystrix方式命令请求==============");
        OrderServiceCommand command = new OrderServiceCommand("orederGroup", restTemplate, id);
        User user = command.execute();
        return user;
    }

    /**
     * 通过注解HystrixCommand方法熔断, 限流, 超时, 降级
     * 注解方式除了在方法上加HystrixCommand, 还要在main方法上加EnableCircuitBreaker, 否则不生效, command不需要这样
     *
     * @param id
     * @return
     */
    @HystrixCommand(fallbackMethod = "findByIdFallback")
    @GetMapping("/user/annotation/{id}")
    public User getUserByAnnotation(@PathVariable Long id) {
        LOGGER.info("================Hystrix方式注解请求==============");
        return this.restTemplate.getForObject("http://microservice-provider-user/hystrix/" + id, User.class);
    }

    public User findByIdFallback(Long id) {
        User user = new User();
        user.setId(-1L);
        user.setName("默认用户");
        return user;
    }
}
