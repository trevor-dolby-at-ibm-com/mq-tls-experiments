/*                                                                        */
/* Based on https://github.com/cicsdev/cics-java-liberty-springboot-jms   */
/*                                                                        */
/* (c) Copyright IBM Corp. 2020, 2026 All Rights Reserved                 */
/*                                                                        */


package com.ibm.experiments.springmq;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.scheduling.concurrent.DefaultManagedTaskExecutor;

/**
 * 
 * This class is the entry point of the spring boot application which contains @SpringBootApplication annotation and the main method to run the Spring Boot application.
 * 
 * A single @SpringBootApplication annotation can be used to enable those three features, that is:
 *
 *   @EnableAutoConfiguration: enable Spring Boot’s auto-configuration mechanism
 *   @ComponentScan: scan all the beans and package declarations when the application initializes.
 *   @Configuration: allow to register extra beans in the context or import additional configuration classes
 * 
 * @EnableJms: enable JMS listener annotated endpoints.
 */
@SpringBootApplication
@EnableJms
public class JMSApplication 
{
    
    public static void main(String[] args) 
    {    
        SpringApplication.run(JMSApplication.class, args);    
    }

    
    /**
     * Lookup the JMS connection factory and create the Spring bean
     * 
     * @return, the connection factory from Liberty
     */
    //@Bean
    public ConnectionFactory connectionFactory() 
    {    
        return null;
    }
    

    /**
     * Create JMS listener for the MDP, setting the CF, task executor, and Txn Mgr
     * from Spring beans.
     * 
     * @param connectionFactory, the connection factory from Liberty
     * @return a JMS listener container from the factory for the MDP
     */
    //@Bean
    public JmsListenerContainerFactory<?> myFactoryBean(ConnectionFactory connectionFactory) 
    {    
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        
        // For JCICS integration it is important to set the JMSListenerContainerFactory to
        // the DefaultManagedTaskExecutor
        factory.setTaskExecutor(taskExecutor());
        
        return factory;
    }

    /**
     * Ensure we supply Spring with Liberty executor threads which are CICS enabled by default
     * 
     * @return the DefaultManagedTaskExecutor
     */
    //@Bean
    public TaskExecutor taskExecutor() 
    {    
        return new DefaultManagedTaskExecutor();
    }

}