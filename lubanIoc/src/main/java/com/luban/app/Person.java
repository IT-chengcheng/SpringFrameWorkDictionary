package com.luban.app;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class Person implements  FactoryBean{


	@Nullable
	@Override
	public Object getObject() throws Exception {
		return new Dog();
	}

	@Nullable
	@Override
	public Class<?> getObjectType() {
		return Dog.class;
	}
}
