package com.team25.backend.domain.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {
    @GetMapping("/login")
    public String loginP() {

        return "admin/login";
    }
}
