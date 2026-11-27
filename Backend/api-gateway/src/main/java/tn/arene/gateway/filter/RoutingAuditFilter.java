package tn.arene.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Trace l'instance amont réellement sélectionnée et l'algorithme utilisé, juste après le filtre
 * de load balancing de Spring Cloud Gateway.
 */
@Component
public class RoutingAuditFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RoutingAuditFilter.class);

    private final String hashHeader;

    public RoutingAuditFilter(@Value("${gateway.lb.hash.header:X-Route-Key}") String hashHeader) {
        this.hashHeader = hashHeader;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Response<ServiceInstance> lbResponse =
                exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

        if (lbResponse != null && lbResponse.hasServer()) {
            ServiceInstance instance = lbResponse.getServer();
            String routeId = route == null ? instance.getServiceId() : route.getId();
            log.debug("Routing decision: route={}, service={}, algorithm={}, upstream={}:{}",
                    routeId,
                    instance.getServiceId(),
                    algorithmFor(routeId),
                    instance.getHost(),
                    instance.getPort());
        }
        return chain.filter(exchange);
    }

    private String algorithmFor(String routeId) {
        return switch (routeId) {
            case "participants" -> "Random";
            case "ms1-challenges" -> "HeaderHash(" + hashHeader + ")";
            default -> "RoundRobin(default)";
        };
    }

    @Override
    public int getOrder() {
        return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 1;
    }
}
