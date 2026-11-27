package tn.arene.gateway;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.context.ApplicationContext;

import tn.arene.loadbalancer.ChallengeHashLoadBalancerConfig;
import tn.arene.loadbalancer.ParticipantLoadBalancerConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class ApiGatewayApplicationTests {

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(routeLocator).isNotNull();
    }

    @Test
    void allSixServicesAreRoutedThroughEureka() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Map<String, Route> byId = routes.stream().collect(Collectors.toMap(Route::getId, route -> route));
        assertThat(byId).containsOnlyKeys(
                "ms1-challenges", "participants", "ms3-soumissions",
                "ms4-evaluation", "ms5-verification", "ms6-reputation");

        assertThat(byId.get("ms1-challenges").getUri().toString()).isEqualTo("lb://ms1-challenges");
        assertThat(byId.get("participants").getUri().toString()).isEqualTo("lb://participants");
        assertThat(byId.get("ms3-soumissions").getUri().toString()).isEqualTo("lb://ms3-soumissions");
        assertThat(byId.get("ms4-evaluation").getUri().toString()).isEqualTo("lb://ms4-evaluation");
        assertThat(byId.get("ms5-verification").getUri().toString()).isEqualTo("lb://ms5-verification");
        assertThat(byId.get("ms6-reputation").getUri().toString()).isEqualTo("lb://ms6-reputation");

        assertThat(byId.get("participants").getPredicate().toString()).contains("/ms2/**");
        assertThat(byId.get("participants").getFilters()).hasSize(1);
        assertThat(byId.get("ms6-reputation").getFilters()).hasSize(2);
    }

    @Test
    void loadBalancerAlgorithmsAreScopedToTheRightServices() {
        LoadBalancerClients clients =
                ApiGatewayApplication.class.getAnnotation(LoadBalancerClients.class);

        assertThat(clients).isNotNull();
        Map<String, Class<?>[]> configurations = Arrays.stream(clients.value())
                .collect(Collectors.toMap(LoadBalancerClient::name, LoadBalancerClient::configuration));

        assertThat(configurations).containsOnlyKeys("participants", "ms1-challenges");
        assertThat(configurations).containsEntry(
                "participants", new Class<?>[] { ParticipantLoadBalancerConfig.class });
        assertThat(configurations).containsEntry(
                "ms1-challenges", new Class<?>[] { ChallengeHashLoadBalancerConfig.class });

        // Le contexte principal ne doit contenir aucun ReactorLoadBalancer : sinon un bean
        // personnalisé serait partagé par tous les services au lieu des deux services ciblés.
        assertThat(applicationContext.getBeansOfType(ReactorLoadBalancer.class)).isEmpty();
    }
}
