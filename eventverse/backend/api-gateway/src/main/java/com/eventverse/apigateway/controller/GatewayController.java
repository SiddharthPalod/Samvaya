package com.eventverse.apigateway.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {
    @GetMapping("/gateway/ping")
    public String ping() {
        return "api-gateway-ok";
    }
}
