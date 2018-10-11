package com.czw.order.command;

import com.czw.order.entity.User;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.springframework.web.client.RestTemplate;

public class OrderServiceCommand extends HystrixCommand<User> {

    private RestTemplate restTemplate;
    private Long id;

    public OrderServiceCommand(String commandGroupKey, RestTemplate restTemplate, Long id) {
    	/*super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("testGroup"))
    				.andCommandKey(HystrixCommandKey.Factory.asKey("testCommand"))
    				.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("testThreadPool")));*/
        super(HystrixCommandGroupKey.Factory.asKey(commandGroupKey));
        this.restTemplate = restTemplate;
        this.id = id;
    }

    //服务调用
    @Override
    protected User run() {
        System.out.println(Thread.currentThread().getName());
        return restTemplate.getForObject("http://microservice-provider-user/hystrix/" + id, User.class);
    }

    //服务降级时所调用的Fallback()
    @Override
    protected User getFallback() {
        User user = new User();
        user.setId(-1L);
        user.setName("降级用户");
        return user;
    }
}