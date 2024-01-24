package kasun.dev.orderservice.service;


import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import kasun.dev.orderservice.dto.*;
import kasun.dev.orderservice.event.OrderPlacedEvent;
import kasun.dev.orderservice.model.Order;
import kasun.dev.orderservice.model.OrderLineItems;
import kasun.dev.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;



import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;


    @Transactional
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemList = orderRequest.getOrderLineItemDtoList().stream().
                map(this::mapToEntity).toList();
        order.setOrderLineItemsList(orderLineItemList);

        List<String> skuCodeList = orderLineItemList.stream().map(OrderLineItems::getSkuCode).toList();

        Span inventoryServiceLookup=tracer.nextSpan().name("InventoryServiceLookup");

        try(Tracer.SpanInScope spanInScope=tracer.withSpan(inventoryServiceLookup.start())) {
            InventoryInStockResponse[] result = webClientBuilder.build().get().
                    uri("http://inventory-service/api/inventory"
                            , uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodeList)
                                    .build()).
                    retrieve()
                    .bodyToMono(InventoryInStockResponse[].class)
                    .block();

            boolean allProductsInStock = Arrays.stream(result).allMatch(InventoryInStockResponse::isInStock);
            if (allProductsInStock) {
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic",
                        new OrderPlacedEvent(order.getOrderNumber()));
                return "Order placed successfully";
            } else {
                throw new IllegalArgumentException();
            }
        }
        finally{
            inventoryServiceLookup.end();
        }


    }

    private OrderLineItems mapToEntity(OrderLineItemDto dto){
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setQuantity(dto.getQuantity());
        orderLineItems.setPrice(dto.getPrice());
        orderLineItems.setSkuCode(dto.getSkuCode());
        return orderLineItems;
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders=orderRepository.findAll();
        return orders.stream().map(this::mapToResponse).toList();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder().orderNumber(order.getOrderNumber())
                .orderLineItemsList(order.getOrderLineItemsList().stream().map(this::mapToResponse).toList()).build();
    }

    private OrderLineItemsResponse mapToResponse(OrderLineItems orderLineItem){
        return OrderLineItemsResponse.builder().skuCode(orderLineItem.getSkuCode())
                .price(orderLineItem.getPrice())
                .quantity(orderLineItem.getQuantity()).build();
    }
}
