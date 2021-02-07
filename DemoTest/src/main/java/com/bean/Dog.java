package com.bean;

import com.annotation.Eat;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author:chengcheng
 * @date:2020.12.30
 */
@Eat
@Component
@Transactional
public class Dog {


}
