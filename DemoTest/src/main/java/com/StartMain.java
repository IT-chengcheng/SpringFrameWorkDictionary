package com;

import com.annotation.Eat;
import com.config.CglibConfig;
import com.config.Configure;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * @author:chengcheng
 * @date:2020.12.30
 */
public class StartMain {

	public static void main(String[] args) {
		//ClassPathXmlApplicationContext d = new ClassPathXmlApplicationContext();
		AnnotationConfigApplicationContext c = new AnnotationConfigApplicationContext();
		c.register(Configure.class);
		c.refresh();

		System.out.println(c.getBean("person"));

        Map eatMap  =  c.getBeansWithAnnotation(Eat.class);
        System.out.println("eatMap:"+eatMap);
		for (String s : c.getBeanFactory().getBeanDefinitionNames()) {
			System.out.println("bd:"+s);
		}
	}

	void  testCglib(){
		Enhancer enhancer = new Enhancer();
		//增强父类，地球人都知道cglib是基于继承来的
		enhancer.setSuperclass(CglibConfig.class);
		//不继承Factory接口
		enhancer.setUseFactory(false);
		enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
		/**
		 * BeanFactoryAwareGeneratorStrategy是一个生成策略
		 * 主要为生成的CGLIB类中添加成员变量$$beanFactory
		 * 同时基于接口EnhancedConfiguration的父接口BeanFactoryAware中的setBeanFactory方法，
		 * 设置此变量的值为当前Context中的beanFactory,这样一来我们这个cglib代理的对象就有了beanFactory
		 * 有了factory就能获得对象，而不用去通过方法获得对象了，因为通过方法获得对象不能控制器过程
		 * 该BeanFactory的作用是在this调用时拦截该调用，并直接在beanFactory中获得目标bean
		 * enhancer.setStrategy(new ConfigurationClassEnhancer.BeanFactoryAwareGeneratorStrategy(classLoader));
		 * 过滤方法，不能每次都去new
		 */
		enhancer.setCallback(new Callback(){

		});
		CglibConfig chil = (CglibConfig) enhancer.create();
		chil.testProxyMethod();

	}
}
