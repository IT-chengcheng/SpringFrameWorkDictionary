package com.luban.app;

import com.luban.anno.EanbleLuabn;
import com.luban.dao.Dao;
import com.luban.dao.IndexDao;
import com.luban.dao.IndexDao1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

@Component
public class Person {



	//@Autowired
	Dog dao;

	public Dog getDao() {
		System.out.println("get 方法");
		return dao;
	}

	@Autowired
	public void ewewe(Dog dao) {
		System.out.println("set 方法");
		this.dao = dao;
	}
}
