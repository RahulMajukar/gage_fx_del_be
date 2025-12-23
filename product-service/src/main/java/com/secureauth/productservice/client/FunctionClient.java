package com.secureauth.productservice.client;

import com.secureauth.productservice.client.dto.FunctionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "secure-product-api", contextId = "functionClient", path = "/api/functions")
public interface FunctionClient {
    @GetMapping
    List<FunctionDTO> list();
}


