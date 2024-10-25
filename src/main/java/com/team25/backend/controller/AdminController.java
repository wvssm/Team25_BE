package com.team25.backend.controller;

import com.team25.backend.dto.response.AdminPageResponse;
import com.team25.backend.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String showAdminPage(Model model) {
        List<AdminPageResponse> usersWithManagers = adminService.getAllUsersWithManagers();
        model.addAttribute("usersWithManagers", usersWithManagers);
        return "admin/list";
    }

    @PostMapping("/admin/changeRole")
    public String changeUserRole(@RequestParam("userId") Long userId, @RequestParam("role") String role) {
        adminService.changeUserRole(userId, role);
        return "redirect:/admin";
    }
}
