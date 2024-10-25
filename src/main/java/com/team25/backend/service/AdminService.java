package com.team25.backend.service;

import com.team25.backend.dto.response.AdminPageResponse;
import com.team25.backend.entity.Manager;
import com.team25.backend.entity.User;
import com.team25.backend.repository.UserRepository;
import com.team25.backend.entity.Certificate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AdminPageResponse> getAllUsersWithManagers() {
        return userRepository.findUsersWithManager().stream()
                .map(user -> {
                    Manager manager = user.getManager();
                    List<String> certificateImages = manager.getCertificates().stream()
                            .map(Certificate::getCertificateImage)
                            .collect(Collectors.toList());

                    return new AdminPageResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getRole(),
                            manager.getManagerName(),
                            certificateImages
                    );
                })
                .collect(Collectors.toList());
    }

    public void changeUserRole(Long userId, String role) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.updateRole(role);
            userRepository.save(user);
        }
    }
}
