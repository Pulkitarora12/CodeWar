package com.project.CodeWar.controller;

import com.project.CodeWar.dtos.UserDTO;
import com.project.CodeWar.entity.Role;
import com.project.CodeWar.entity.User;
import com.project.CodeWar.repository.RoleRepository;
import com.project.CodeWar.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "Admin-only user management operations")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepository;

    @Operation(summary = "Get all users")
    @GetMapping("/getusers")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Update user role")
    @PutMapping("/update-role")
    public ResponseEntity<String> updateUserRole(@RequestParam Long userId,
                                                 @RequestParam String roleName) {
        userService.updateUserRole(userId, roleName);
        return ResponseEntity.ok("User role updated");
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Lock or unlock user account")
    @PutMapping("/update-lock-status")
    public ResponseEntity<String> updateAccountLockStatus(@RequestParam Long userId, @RequestParam boolean lock) {
        userService.updateAccountLockStatus(userId, lock);
        return ResponseEntity.ok("Account lock status updated");
    }

    @Operation(summary = "Get all roles")
    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Operation(summary = "Set account expiry status")
    @PutMapping("/update-expiry-status")
    public ResponseEntity<String> updateAccountExpiryStatus(@RequestParam Long userId, @RequestParam boolean expire) {
        userService.updateAccountExpiryStatus(userId, expire);
        return ResponseEntity.ok("Account expiry status updated");
    }

    @Operation(summary = "Enable or disable user account")
    @PutMapping("/update-enabled-status")
    public ResponseEntity<String> updateAccountEnabledStatus(@RequestParam Long userId, @RequestParam boolean enabled) {
        userService.updateAccountEnabledStatus(userId, enabled);
        return ResponseEntity.ok("Account enabled status updated");
    }

    @Operation(summary = "Set credentials expiry status")
    @PutMapping("/update-credentials-expiry-status")
    public ResponseEntity<String> updateCredentialsExpiryStatus(@RequestParam Long userId, @RequestParam boolean expire) {
        userService.updateCredentialsExpiryStatus(userId, expire);
        return ResponseEntity.ok("Credentials expiry status updated");
    }

    @Operation(summary = "Update user password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam Long userId, @RequestParam String password) {
        try {
            userService.updatePassword(userId, password);
            return ResponseEntity.ok("Password updated");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
