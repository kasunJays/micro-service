package kasun.dev.inventoryservice.controller;

import kasun.dev.inventoryservice.Service.InventoryService;
import kasun.dev.inventoryservice.dto.InventoryInStockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku-code}")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@PathVariable("sku-code") String skuCode){
        return inventoryService.isInStock(skuCode);
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryInStockResponse> isInStock(@RequestParam List<String> skuCodes){
        return inventoryService.isInStock(skuCodes);
    }

}
