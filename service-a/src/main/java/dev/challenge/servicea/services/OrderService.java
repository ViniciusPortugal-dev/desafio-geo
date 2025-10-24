package dev.challenge.servicea.services;
import dev.challenge.common.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(OrderDTO dto);
    OrderDTO updateOrder(String id, OrderDTO dto);
    void deleteOrder(String id);
    List<OrderDTO> listOrders();
}

