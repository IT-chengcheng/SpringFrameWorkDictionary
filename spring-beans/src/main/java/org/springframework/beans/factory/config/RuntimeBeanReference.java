/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.config;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Immutable 不变的 placeholder class used for a property value object when it's
 * a reference to another bean in the factory, to be resolved at runtime.。
 *
 * Spring中RuntimeBeanReference的作用：
 *    我们在定义一个BeanDefinition时，可以直接给某些属性赋“值”：
     1. value：值
     2. Reference：beanName

 给属性xx赋值字符串b：
 BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
 builder.addPropertyValue("xx", "b");
 AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
 beanDefinition.setBeanClass(A.class);
 registry.registerBeanDefinition("a", beanDefinition);


 给属性xx赋值一个bean对象，"b"表示bean的名字：
 BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
 builder.addPropertyReference("xx", "b");
 AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
 beanDefinition.setBeanClass(A.class);
 registry.registerBeanDefinition("a", beanDefinition);


 如果属性的类型是一个List，那么该如何赋值呢？可以利用RuntimeBeanReference：
 BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();

 ManagedList<RuntimeBeanReference> runtimeBeanReferences = new ManagedList<>();
 runtimeBeanReferences.add(new RuntimeBeanReference("b1"));
 runtimeBeanReferences.add(new RuntimeBeanReference("b2"));
 builder.addPropertyValue("xx", runtimeBeanReferences);

 AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
 beanDefinition.setBeanClass(A.class);
 registry.registerBeanDefinition("a", beanDefinition);


 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
public class RuntimeBeanReference implements BeanReference {

	private final String beanName;

	private final boolean toParent;

	@Nullable
	private Object source;


	/**
	 * Create a new RuntimeBeanReference to the given bean name,
	 * without explicitly marking it as reference to a bean in
	 * the parent factory.
	 * @param beanName name of the target bean
	 */
	public RuntimeBeanReference(String beanName) {
		this(beanName, false);
	}

	/**
	 * Create a new RuntimeBeanReference to the given bean name,
	 * with the option to mark it as reference to a bean in
	 * the parent factory.
	 * @param beanName name of the target bean
	 * @param toParent whether this is an explicit reference to
	 * a bean in the parent factory
	 */
	public RuntimeBeanReference(String beanName, boolean toParent) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		this.beanName = beanName;
		this.toParent = toParent;
	}


	@Override
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return whether this is an explicit reference to a bean
	 * in the parent factory.
	 */
	public boolean isToParent() {
		return this.toParent;
	}

	/**
	 * Set the configuration source {@code Object} for this metadata element.
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 */
	public void setSource(@Nullable Object source) {
		this.source = source;
	}

	@Override
	@Nullable
	public Object getSource() {
		return this.source;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RuntimeBeanReference)) {
			return false;
		}
		RuntimeBeanReference that = (RuntimeBeanReference) other;
		return (this.beanName.equals(that.beanName) && this.toParent == that.toParent);
	}

	@Override
	public int hashCode() {
		int result = this.beanName.hashCode();
		result = 29 * result + (this.toParent ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return '<' + getBeanName() + '>';
	}

}
