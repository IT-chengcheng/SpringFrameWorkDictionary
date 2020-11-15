package com.luban.test;


import com.luban.config.Configure;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author:chengcheng
 * @date:2020.10.16
 */
public class Test {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext	c = new AnnotationConfigApplicationContext();

		c.register(Configure.class);

       c.refresh();

		System.out.println(c.getBean("&person"));
		System.out.println(c.getBean("person"));



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
