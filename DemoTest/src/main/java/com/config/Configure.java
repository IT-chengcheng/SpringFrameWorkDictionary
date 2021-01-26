package com.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author:chengcheng
 * @date:2020.10.17
 */
@ComponentScan({"com.Bean","com.dao"})
@Configuration
//@ImportResource("classpath:spring.xml")
public class Configure {
}
