package com.czw.user.controller;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.czw.user.entity.User;
import com.czw.user.repository.UserRepository;
import org.apache.log4j.Logger;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Registration registration;

    @Autowired
    private RedissonClient  redissonClient;

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) throws Exception {
        return userRepository.findOne(id);
    }

    @GetMapping("/getIpAndPort")
    public String findById() {
        return registration.getHost() + ":" + registration.getPort();
    }


    /**
     * 为用户添加年龄
     * 测试分布式锁, 没有使用分布式锁时
     * @param id
     * @param age
     * @return
     * @throws Exception
     */
    @GetMapping("/user/{id}/{age}")
    public User modifyUserAge(@PathVariable Long id,
                            @PathVariable Integer age) {
        synchronized(userRepository) {
            User user = userRepository.findOne(id);
            logger.info("用户老的年龄为:" + user.getAge());
            user.setAge(user.getAge() + age);
            userRepository.save(user);
            logger.info("用户新的年龄为:" + user.getAge());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return user;
        }
    }


    /**
     * 分布式锁的方式控制
     * 如果上锁时间少于执行业务时间则会返回锁, 使用lock.lock()可以解决这个问题,
     * 虽然lock()是阻塞方式, 如果怕lock阻塞太多, 可以在lick()前加一个AtomicInteger()记录, 大于某一个数, 则提示用户稍后再试, 限流
     * 或者设置上锁后自动解锁的时间长一点, 例如 1分钟
     * @param id
     * @param age
     * @return
     */
    @GetMapping("/user/distributed/{id}/{age}")
    public User modifyUserAgeByDistributed(@PathVariable Long id,
                            @PathVariable Integer age) {
        logger.info("分布式锁测试开始");
        RLock lock = redissonClient.getLock("user");
            User user = null;
            try {
                // 尝试加锁，最多等待100秒，上锁以后5秒自动解锁
                if (lock.tryLock(100, 60, TimeUnit.SECONDS)) {
                    lock.lock();
                    user = userRepository.findOne(id);
                    logger.info("用户老的年龄为:" + user.getAge());
                    user.setAge(user.getAge() + age);
                    userRepository.save(user);
                    logger.info("用户新的年龄为:" + user.getAge());
                    //测试超时
                    Thread.sleep(2000);
                 }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                logger.info("分布式锁测试结束");
                return user;
            }

    }

    /**
     * Hystrix熔断测试
     *
     * @param id
     * @return
     * @throws Exception
     */
    @GetMapping("/hystrix/{id}")
    public User getUser(@PathVariable Long id) throws Exception {
        logger.info("用户中心接口：查询用户" + id + "信息");
        //测试超时触发降级
        int sleepTime = new Random().nextInt(10000);
        logger.info("sleepTime:" + sleepTime);
        Thread.sleep(sleepTime);

        //测试熔断，传入不存在的用户id模拟异常情况
        //if (id == 10) {
        //	 throw new NullPointerException();
        //}

        //测试限流,线程资源隔离,模拟系统执行速度很慢的情况
        //Thread.sleep(3000);

        return userRepository.findOne(id);
    }
}
