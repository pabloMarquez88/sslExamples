package com.oneway.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server-ssl")
public class ServerSslController {
    @GetMapping("/test")
    public String testSSL()  {
        return "Hello from SSL server";
    }
}