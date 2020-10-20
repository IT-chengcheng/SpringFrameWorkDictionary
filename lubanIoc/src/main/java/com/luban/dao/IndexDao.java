package com.luban.dao;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class IndexDao implements Dao, Serializable, FactoryBean {


	/**
	 * 取钱
	 */
	public void query(){
		System.out.println("index");
		System.out.println("index");
		System.out.println("index");
		System.out.println("index");
		System.out.println("index");
		System.out.println("index");
		System.out.println("index");

	}


	@Override
	public Object getObject() throws Exception {
		return new IndexDao1();
	}

	@Override
	public Class<?> getObjectType() {
		return null;
	}
}
