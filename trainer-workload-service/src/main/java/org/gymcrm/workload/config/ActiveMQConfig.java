package org.gymcrm.workload.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActiveMQConfig {

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory(
            @Value("${spring.activemq.broker-url}") String brokerUrl,
            @Value("${spring.activemq.user}") String user,
            @Value("${spring.activemq.password}") String password,
            @Value("${activemq.redelivery.max-attempts:1}") int maxRedeliveries) {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(user, password, brokerUrl);

        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(maxRedeliveries);
        redeliveryPolicy.setInitialRedeliveryDelay(1000);
        factory.setRedeliveryPolicy(redeliveryPolicy);

        return factory;
    }
}