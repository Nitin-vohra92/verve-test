package com.example.verve_test.controller;

import com.example.verve_test.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/verve")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @GetMapping("/accept")
    public Mono<String> acceptRequest(
            @RequestParam Integer id,
            @RequestParam(required = false) String endpoint) {

        return requestService.processRequest(id, endpoint)
                .map(success -> success ? "ok" : "failed")
                .onErrorReturn("failed");
    }
}