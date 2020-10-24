package com.luban.test;

import com.luban.app.Dog;
import com.luban.app.Person;
import com.luban.config.Configure;
import com.luban.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author:chengcheng
 * @date:2020.10.16
 */
public class Test {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext	c = new AnnotationConfigApplicationContext(Configure.class);

		System.out.println(c.getBean("car"));

		/*AnnotationConfigApplicationContext	a = new AnnotationConfigApplicationContext();
        a.register(Configure.class);
        a.refresh();

		ClassPathXmlApplicationContext d = new ClassPathXmlApplicationContext();*/

	}

	
}
