package com.chatokjunior.secureteamapi.manager;

import com.chatokjunior.secureteamapi.manager.dto.ManagerDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/managers")
@RestController
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ManagerDashboardResponse> managerDashboard() {
        return ResponseEntity.ok(managerService.getDashboard());
    }
}
