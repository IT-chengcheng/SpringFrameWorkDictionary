/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		// 存放的是所有已经执行了的processor。看这段英文翻译过来就是：优先执行BeanDefinitionRegistryPostProcessors接口类
		// 然后执行他的父类接口BeanFactoryPostProcessor的实现类
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			//程序员手动加入的beanFactoryPostProcessors，如果没有自定义BeanFactoryPostProcessor，那么这个数组就是空
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					// 将程序员自定义的BFP添加到这是个数组中，下面还会把spring的BFP也加到这个数组中，合并到一块
					registryProcessors.add(registryProcessor);
				}
				else {//BeanDefinitionRegistryPostProcessor  BeanfactoryPostProcessor
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.

			//这个currentRegistryProcessors 放的是spring内部自己实现了BeanDefinitionRegistryPostProcessor接口的对象
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			//BeanDefinitionRegistryPostProcessor 继承 BeanFactoryPostProcessor
			//getBeanNamesForType  根据bean的类型获取bean的名字只会获取到一个：ConfigurationClassPostProcessor
			// 这个ConfigurationClassPostProcessor是在reader的构造方法中加进去的，伴随着那一堆beanPostProcessor一块加进去的
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			//这个地方可以得到一个BeanFactoryPostProcessor，因为是spring默认在最开始自己注册的
			//为什么要在最开始注册这个呢？
			//因为spring的工厂需要许解析去扫描等等功能
			//而这些功能都是需要在spring工厂初始化完成之前执行
			//要么在工厂最开始的时候、要么在工厂初始化之中，反正不能再之后
			//因为如果在之后就没有意义，因为那个时候已经需要使用工厂了
			//所以这里spring'在一开始就注册了一个BeanFactoryPostProcessor，用来插手springfactory的实例化过程
			//在这个地方断点可以知道这个类叫做ConfigurationClassPostProcessor
			//ConfigurationClassPostProcessor 做了超级多的事
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					// 执行beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class)，意味着就已经这个类实例化了
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			//排序不重要，况且currentRegistryProcessors这里也只有一个数据
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			//合并list，(为什么要合并，因为还有自己的)，上面已经加了程序员自定义的了
			// registryProcessors目前只有自己的   currentRegistryProcessors是spring自定义的，俩合并一起
			registryProcessors.addAll(currentRegistryProcessors);
			//最重要。注意这里是方法调用
			//执行所有BeanDefinitionRegistryPostProcessor，其实currentRegistryProcessors这个数组里就一个，就是spring自己的那个
            //-->>startScan3
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			//执行完成了所有BeanDefinitionRegistryPostProcessor
			//这个list只是一个临时变量，故而要清除，下面会执行多次，因为要把之前执行过的从数组中移除，然后把新的加进去
			currentRegistryProcessors.clear();

			/**
			 * 执行完上面的startScan3，意味着就已经完成了所有的扫描，所有加了注解的类都已经变成bd放到了beanfactory的map中了
			 */
			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
				// 执行完for循环，currentRegistryProcessors又有BeanfactoryPostprocess了，这些BeanfactoryPostprocess是扫描出来的
				//何为扫描出来的？ 就是实现了BeanDefinitionRegistryPostProcessor接口，并且加了@Component注解的，并不是手动添加的
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			// 这一步做的很全面，防止执行完上一步后，又加入了新的BeanDefinitionRegistryPostProcessor，做到疏而不漏
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			//执行BeanFactoryPostProcessor的回调，前面不是吗？
			//前面执行的BeanFactoryPostProcessor的子类BeanDefinitionRegistryPostProcessor的回调
			//这是执行的是BeanFactoryPostProcessor    postProcessBeanFactory
			/**
			 * 这里面有三个数组
			 * 1、regularPostProcessors里面存放的是程序员手动添加的BeanFactoryPostProcessor，非BeanDefinitionRegistryPostProcessor
			 *      而且不包括扫描出来的
			 * 2、registryProcessors 存放的是所有的BeanDefinitionRegistryPostProcessor，spring的，程序员的，以及扫描出来的
			 *      spring的就只有ConfuguratuonClassPostProcssor，也就是说程序员没加的话，扫描包没有的话，这个数组始终就一个
			 * 3、currentRegistryProcessors里面只放BeanDefinitionRegistryPostProcessor，放进去，接着执行，然后清空，然后再放，只是个临时变量
			 * 再次强调：前面所有的代码目的是为了执行BeanDefinitionRegistryPostProcessor接口的拓展方法postProcessBeanDefinitionRegistry
			 * 下面这两行才是执行BeanFactoryPostProcessor的接口方法postProcessBeanFactory、
			 * 而且是分开了执行，首先执行了子接口的实现类BeanDefinitionRegistryPostProcessor ->postProcessBeanFactory()
			 *                   再执行BeanFactoryPostProcessor的实现类 ->postProcessBeanFactory()
			 */
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);

			/**
			 * 执行完这两个方法后，
			 * 扫描包中的BeanDefinitionRegistryPostProcessor接口类都已经执行完毕
			 * 扫描包中的BeanFactoryPostProcessor接口类还未执行
			 *
			 * 这里说的执行，指的是：BeanFactoryPostProcessor的方法 postProcessBeanFactory()
			 * BeanDefinitionRegistryPostProcessor 继承 BeanFactoryPostProcessor，拓展了一个方法postProcessBeanDefinitionRegistry（）
			 * 这个方法已经全部执行完毕
			 */
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		//ConfigurationClassPostProcessor
		/**
		 * 从下面开始的代码，就是将扫描包中扫描出来的所有BeanFactoryPostProcessor，提取出来，执行。
		 * 但是执行的时候，有一个判断会，过滤掉之前已经执行完的BeanFactoryPostProcessor，
		 * 以及他的子类BeanDefinitionRegistryPostProcessor。也就是说，接着来就是执行剩下的扫描包中纯BeanFactoryPostProcessor
		 */
		// 程序员手动add（）的，通过这个方法是取不出来的，因为自己加的不会封装成bd，也就不会加到beanfactory的map中
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
				// 最开始上面的每一步的代码，只要执行了PostProcessor了的，接口类，都放到了这个数组里了
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
		/**
		 * registerBeanPostProcessors中注册BeanPostProcessor的顺序是：
		   1、注册实现了PriorityOrdered接口的BeanPostProcessor
		   2、注册实现了Ordered接口的BeanPostProcessor
		   3、注册常规的BeanPostProcessor ，也就是没有实现优先级接口的BeanPostProcessor
		   4、注册Spring内部BeanPostProcessor
		 */
		//从beanDefinitionMap中得到所有的BeanPostProcessor
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				// SpringAop的这个类 AnnotationAwareAspectJAutoProxyCreator，就在这里加入
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}
		priorityOrderedPostProcessors.remove(1);
		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		internalPostProcessors.remove(1);
		sortPostProcessors(internalPostProcessors, beanFactory);

		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 * 注意对比下面这个方法
	 * BeanDefinitionRegistryPostProcessor和BeanFactoryPostProcessor
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		//因为只有一条数据 就是ConfigurationClassPostProcessor
		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			//-->>startScan4
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 * 当Spring的配置中的后处理器还没有被注册就已经开始了bean的初始化
	 *	便会打印出BeanPostProcessorChecker中设定的信息
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
