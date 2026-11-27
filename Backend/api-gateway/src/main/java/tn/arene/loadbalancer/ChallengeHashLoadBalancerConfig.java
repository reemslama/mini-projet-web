package tn.arene.loadbalancer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Contexte enfant du service MS1 : remplace Round Robin par Header Hash. */
@Configuration(proxyBeanMethods = false)
public class ChallengeHashLoadBalancerConfig {

    @Bean
    ReactorLoadBalancer<ServiceInstance> headerHashLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory factory,
            @Value("${gateway.lb.hash.header:X-Route-Key}") String headerName) {
        String serviceId = environment.getProperty(
                LoadBalancerClientFactory.PROPERTY_NAME, "ms1-challenges");
        return new ChallengeHashLoadBalancer(
                factory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                serviceId,
                headerName);
    }
}
