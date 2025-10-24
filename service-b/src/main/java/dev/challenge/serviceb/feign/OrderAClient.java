package dev.challenge.serviceb.feign;

import dev.challenge.common.configuration.ReplicationFeignConfig;
import dev.challenge.common.dto.OrderDTO;
import dev.challenge.common.dto.OrderReplicaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "order-a", url = "${service.a.url}", configuration = ReplicationFeignConfig.class)
public interface OrderAClient {


    @PostMapping("/pedidos")
    OrderDTO createOrder(@RequestBody OrderReplicaDTO dto);

    @PutMapping("/pedidos/{id}")
    OrderDTO updateOrder(@PathVariable("id") String id, @RequestBody OrderReplicaDTO dto);

    @DeleteMapping("/pedidos/{id}")
    void deleteOrder(@PathVariable("id") String id);
}
