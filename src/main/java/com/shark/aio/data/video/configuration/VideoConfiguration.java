package com.shark.aio.data.video.configuration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoConfiguration implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        VideoConfiguration.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> tClass,String beanName){
        if (applicationContext.containsBean(beanName)){
            return applicationContext.getBean(beanName, tClass);
        }else {
            return null;
        }
    }

    /**
     *  动态注入bean
     * @param requiredType 注入类
     * @param beanName bean名称
     */
    public static void registerBean(Class<?> requiredType,String beanName){

        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;

        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getAutowireCapableBeanFactory();

        //创建bean信息.
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(requiredType);

        //动态注册bean.
        defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());

    }

    public static void unRegistryBean(String beanName) {
//        System.out.println("开始注销bean[{}]。"+beanName);

        try {
            //将applicationContext转换为ConfigurableApplicationContext
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getAutowireCapableBeanFactory();;
            if (beanFactory.containsBeanDefinition(beanName)) {
                beanFactory.destroySingleton(beanName);
                beanFactory.removeBeanDefinition(beanName);
                System.out.println("注销摄像头 " + beanName + " 成功！");
            } else {
                System.out.println("不存在摄像头" + beanName + " 不需要注销！");
            }
        } catch (Exception e){

        }
    }


}
