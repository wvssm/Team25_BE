package com.team25.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AddressController {
    @GetMapping("/address")
    public String roadSearch(){
        return "address/roadSearch";
    }
}
