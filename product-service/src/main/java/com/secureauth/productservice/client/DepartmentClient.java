package com.secureauth.productservice.client;

import com.secureauth.productservice.client.dto.DepartmentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "secure-product-api", contextId = "departmentClient", path = "/api/departments")
public interface DepartmentClient {
    @GetMapping
    List<DepartmentDTO> list();
}


