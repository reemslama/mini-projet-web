package tn.arene.gateway.fallback;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/** Cible du {@code fallbackUri=forward:/fallback/ms} du filtre CircuitBreaker. */
@RestController
public class FallbackController {

    @GetMapping("/fallback/ms")
    public Map<String, Object> fallback(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "SERVICE_UNAVAILABLE");
        body.put("message", "Le microservice est momentanément indisponible (circuit breaker ouvert).");
        body.put("route", route == null ? "unknown" : route.getId());
        body.put("fallbackUri", "/fallback/ms");
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
