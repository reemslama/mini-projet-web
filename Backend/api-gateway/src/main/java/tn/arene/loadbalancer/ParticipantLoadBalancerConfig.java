package tn.arene.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Stratégie Random pour MS2 Participants ({@code participants}), le service « Candidat » de
 * l'atelier.
 *
 * <p>Cette classe est volontairement dans {@code tn.arene.loadbalancer}, hors du package scanné
 * par {@code tn.arene.gateway}. Elle est donc uniquement chargée dans le contexte enfant créé par
 * {@code @LoadBalancerClient}, et jamais dans le contexte principal : les cinq autres services
 * conservent ainsi Round Robin.</p>
 */
@Configuration(proxyBeanMethods = false)
public class ParticipantLoadBalancerConfig {

    @Bean
    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory factory) {
        String serviceId = environment.getProperty(
                LoadBalancerClientFactory.PROPERTY_NAME, "participants");
        return new RandomLoadBalancer(
                factory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class), serviceId);
    }
}
