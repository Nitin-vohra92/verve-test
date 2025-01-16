package com.example.verve_test.controller;

import com.example.verve_test.service.WebClientLoadTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class WebClientLoadTestController {

    @Autowired
    private WebClientLoadTestService loadTestService;

    @GetMapping("/loadTest")
    public String loadTest(@RequestParam int numberOfRequests) {
        List<Mono<Void>> requests = new ArrayList<>();

        String baseUrl = "/api/verve/accept";

        for (int i = 0; i < numberOfRequests; i++) {
            int randomQueryParam = ThreadLocalRandom.current().nextInt(1, 101);
            String url = baseUrl + "?id=" + randomQueryParam;

            Mono<Void> request = loadTestService.makeApiRequest(url)
                    .then();
            if (request != null) {
                requests.add(request);
            } else {
                requests.add(Mono.empty());
            }
        }

        try{
            Mono.when(requests).block();  // Blocking the current thread to wait for all requests

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Load Test completed with " + numberOfRequests + " requests.";
    }
}
