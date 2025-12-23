package com.secureauth.productservice.client;

import com.secureauth.productservice.client.dto.OperationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "secure-product-api", contextId = "operationClient", path = "/api/operations")
public interface OperationClient {
    @GetMapping
    List<OperationDTO> list();
}


