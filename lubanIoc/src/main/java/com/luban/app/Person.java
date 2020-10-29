package com.luban.app;

import com.luban.anno.EanbleLuabn;
import com.luban.dao.Dao;
import com.luban.dao.IndexDao;
import com.luban.dao.IndexDao1;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class Person implements FactoryBean{





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
