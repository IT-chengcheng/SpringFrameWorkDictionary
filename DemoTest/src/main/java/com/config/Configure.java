package com.config;

import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author:chengcheng
 * @date:2020.10.17
 */
@ComponentScan({"com.Bean","com.beanPostProcessor"})
@Configuration
//@ImportResource("classpath:spring.xml")
@EnableTransactionManagement
public class Configure {

}
