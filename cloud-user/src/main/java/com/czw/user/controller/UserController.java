package com.czw.user.controller;

import com.czw.user.entity.User;
import com.czw.user.repository.UserRepository;
import org.apache.log4j.Logger;
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
  

  @GetMapping("/{id}")
  public User findById(@PathVariable Long id) throws Exception {
      return userRepository.findOne(id);
  }
  
  @GetMapping("/getIpAndPort")
  public String findById() {
	  return registration.getHost() + ":" + registration.getPort();
  }
}
