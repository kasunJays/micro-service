package kasun.dev.inventoryservice.Service;


import kasun.dev.inventoryservice.dto.InventoryInStockResponse;
import kasun.dev.inventoryservice.model.Inventory;
import kasun.dev.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;


    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode){
        return inventoryRepository.findBySkuCode(skuCode).isPresent();
    }

    @SneakyThrows
    @Transactional
    public List<InventoryInStockResponse> isInStock(List<String> skuCodes) {
        log.info("Wait started");
       // Thread.sleep(10000);
        log.info("Wait ended");
        return inventoryRepository.findBySkuCodeIn(skuCodes).stream()
                 .map(this::mapToDto).toList();
    }

    private InventoryInStockResponse mapToDto(Inventory inventory){
        return InventoryInStockResponse.builder().skuCode(inventory.getSkuCode())
                .isInStock(inventory.getQuantity()>0).build();
    }
}
