package com.team25.backend.controller;

import com.team25.backend.dto.response.AdminPageResponse;
import com.team25.backend.entity.Manager;
import com.team25.backend.service.AdminService;
import com.team25.backend.service.ManagerService;
import com.team25.backend.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;
    private final ManagerService managerService;

    public AdminController(AdminService adminService, UserService userService, ManagerService managerService) {
        this.adminService = adminService;
        this.userService = userService;
        this.managerService = managerService;
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

    @PostMapping("/admin/manager")
    public String removeUserByUserId(@RequestParam("userId") Long userId) {
        userService.removeUser(userId);
        return "redirect:/admin"; // 관리자 페이지로 리다이렉트
    }

    @GetMapping("admin/managers")
    public String showAllManagersPage(Model model) {
        List<Manager> managers = managerService.getManagersWithCertificatesAndWorkingHour();
        model.addAttribute("managers", managers);
        return "admin/managerList";
    }
}
