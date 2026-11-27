package tn.arene.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Mono;

/**
 * Algorithme "Header Hash" : l'instance est choisie de façon déterministe par
 * {@code Math.abs(hashCode(X-Route-Key)) % nombre d'instances}. Ce n'est ni Round Robin ni
 * Random : la même clé mène toujours à la même instance, ce qui permet un routage "sticky"
 * sans session côté Gateway.
 *
 * <p>Appliqué uniquement à MS1 ({@code ms1-challenges}) pour satisfaire la partie
 * « autre algorithme sur un deuxième MS » de l'atelier.</p>
 */
public class ChallengeHashLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Logger log = LoggerFactory.getLogger(ChallengeHashLoadBalancer.class);

    private final ObjectProvider<ServiceInstanceListSupplier> supplierProvider;
    private final String serviceId;
    private final String headerName;

    public ChallengeHashLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> supplierProvider,
            String serviceId,
            String headerName) {
        this.supplierProvider = supplierProvider;
        this.serviceId = serviceId;
        this.headerName = headerName;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = supplierProvider.getIfAvailable();
        if (supplier == null) {
            return Mono.just(new EmptyResponse());
        }
        return supplier.get().next().map(instances -> chooseInstance(instances, request));
    }

    private Response<ServiceInstance> chooseInstance(
            java.util.List<ServiceInstance> instances, Request request) {
        if (instances.isEmpty()) {
            return new EmptyResponse();
        }

        String key = extractKey(request);
        int index = Math.floorMod(key.hashCode(), instances.size());
        ServiceInstance instance = instances.get(index);

        log.debug("HeaderHash LB [{}] key='{}' -> {} ({} instances)",
                serviceId, key, instance.getUri(), instances.size());
        return new DefaultResponse(instance);
    }

    private String extractKey(Request request) {
        Object context = request.getContext();
        if (context instanceof RequestDataContext requestDataContext) {
            HttpHeaders headers = requestDataContext.getClientRequest().getHeaders();
            String headerValue = headers.getFirst(headerName);
            if (StringUtils.hasText(headerValue)) {
                return headerValue;
            }
            // Clé stable par défaut : le chemin appelé, pour rester déterministe sans header.
            return requestDataContext.getClientRequest().getUrl().getPath();
        }
        return serviceId;
    }
}
