package com.secureauth.productservice.client;

import com.secureauth.productservice.client.dto.CreateEventRequest;
import com.secureauth.productservice.client.dto.EventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "calendar-service",           // name of calendar microservice
        contextId = "eventClient",
        path = "/api/calendar/events"          // base path from EventController
)
public interface EventClient {

    @PostMapping
    EventDTO createEvent(
            @RequestBody CreateEventRequest request,
            @RequestHeader("User-ID") Long userId,
            @RequestHeader("User-Email") String userEmail
    );
}
