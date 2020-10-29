package com.luban.test;

import com.luban.app.BeanFactoryPostProcessor;
import com.luban.app.Dog;
import com.luban.app.Eat;
import com.luban.app.Person;
import com.luban.config.Configure;
import com.luban.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * @author:chengcheng
 * @date:2020.10.16
 */
public class Test {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext	c = new AnnotationConfigApplicationContext();

		c.register(Configure.class);
		c.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor());

       c.refresh();

        Dog personName = (Dog) c.getBean("person");
		Person personType =  c.getBean(Person.class);
		Person person = (Person) c.getBean("&person");
		//Dog dogName = (Dog) c.getBean("dog");
		Dog DogType =  c.getBean(Dog.class);


     /* Map temp  =  c.getBeansWithAnnotation(Eat.class);
      System.out.println("temp:"+temp);
		for (String s : c.getBeanFactory().getBeanDefinitionNames()) {
			System.out.println("bd:"+s);
		}*/
		/*AnnotationConfigApplicationContext	a = new AnnotationConfigApplicationContext();
        a.register(Configure.class);
        a.refresh();

		ClassPathXmlApplicationContext d = new ClassPathXmlApplicationContext();*/

	}

	
}
