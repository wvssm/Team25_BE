package com.team25.backend.controller;

import com.team25.backend.annotation.LoginUser;
import com.team25.backend.dto.response.ApiResponse;
import com.team25.backend.dto.response.UserRoleResponse;
import com.team25.backend.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/api/users/me/role")
    @ResponseBody
    public ResponseEntity<ApiResponse<UserRoleResponse>> myAPI(@LoginUser User user){
        UserRoleResponse userRole = new UserRoleResponse(user.getRole());
        return ResponseEntity.ok(new ApiResponse<>(true,"사용자 역할 조회를 성공했습니다.",userRole));
    }
}
