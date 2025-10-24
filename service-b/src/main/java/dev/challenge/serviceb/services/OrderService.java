package dev.challenge.serviceb.services;

import dev.challenge.common.dto.OrderReplicaDTO;
import java.util.List;

public interface OrderService {
    List<OrderReplicaDTO> listOrders();
    OrderReplicaDTO createOrder(OrderReplicaDTO dto);
    OrderReplicaDTO updateOrder(String id, OrderReplicaDTO dto);
    void deleteOrder(String id);
}
