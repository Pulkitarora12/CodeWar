package com.project.CodeWar.service.impl;

import com.project.CodeWar.dtos.UserDTO;
import com.project.CodeWar.entity.AppRole;
import com.project.CodeWar.entity.PasswordResetToken;
import com.project.CodeWar.entity.Role;
import com.project.CodeWar.entity.User;
import com.project.CodeWar.repository.PasswordResetTokenRepository;
import com.project.CodeWar.repository.RoleRepository;
import com.project.CodeWar.repository.UserRepository;
import com.project.CodeWar.security.util.AuthUtil;
import com.project.CodeWar.service.EmailService;
import com.project.CodeWar.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    AuthUtil authUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "userDTOs", key = "'id::' + #id")
    public UserDTO getUserById(Long id) {
        logger.info("Database hit: Fetching UserDTO for id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    private UserDTO convertToDto(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.isAccountNonLocked(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isEnabled(),
                user.getCredentialsExpiryDate(),
                user.getAccountExpiryDate(),
                user.getTwoFactorSecret(),
                user.isTwoFactorEnabled(),
                user.getSignUpMethod(),
                user.getRole(),
                user.getCreatedDate(),
                user.getUpdatedDate()
        );
    }

    @Override
    @Cacheable(cacheNames = "users", key = "'username::' +  #username")
    public User findByUsername(String username) {
        logger.info("Database hit: Fetching User by username: {}", username);
        Optional<User> user = userRepository.findByUserName(username);
        return user.orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @Override
    @Cacheable(cacheNames = "users", key = "'email::' + #email")
    public Optional<User> findByEmail(String email) {
        logger.info("Database hit: Fetching User by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Cacheable(cacheNames = "users", key = "'id::' + #id")
    public User getUserEntityById(Long id) {
        logger.info("Database hit: Fetching User entity for id: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userDTOs"}, allEntries = true)
    public void updatePassword(Long userId, String password) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update password");
        }
    }

    @Override
    @CacheEvict(cacheNames = {"userDTOs", "users"}, allEntries = true)
    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        AppRole appRole = AppRole.valueOf(roleName);
        Role role = roleRepository.findByRoleName(appRole)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    @CacheEvict(cacheNames = {"userDTOs", "users"}, allEntries = true)
    public void updateAccountLockStatus(Long userId, boolean lock) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        user.setAccountNonLocked(!lock);
        userRepository.save(user);
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userDTOs"}, allEntries = true)
    public void updateAccountExpiryStatus(Long userId, boolean expire) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        user.setAccountNonExpired(!expire);
        userRepository.save(user);
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userDTOs"}, allEntries = true)
    public void updateAccountEnabledStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userDTOs"}, allEntries = true)
    public void updateCredentialsExpiryStatus(Long userId, boolean expire) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        user.setCredentialsNonExpired(!expire);
        userRepository.save(user);
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userDTOs"}, allEntries = true)
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (passToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Token has expired");
        }

        if (passToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        User user = passToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        passToken.setUsed(true);

        userRepository.save(user);
        resetTokenRepository.save(passToken);
    }

    @Override
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email).
                orElseThrow(() -> new RuntimeException("User not found"));
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user, expiryDate);
        resetTokenRepository.save(passwordResetToken);

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        emailService.sendResetEmail(email, resetUrl);
    }

    @Override
    @CacheEvict(cacheNames = {"users", "userDTOs"}, allEntries = true)
    public User registerUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUserName(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    @Override
    public Role findRoleByName(AppRole role) {
        Role ans = roleRepository.findByRoleName(role).orElseThrow(() -> new RuntimeException("Role not found"));
        return ans;
    }

    @Override
    public List<Role> getAllRoles() {
        List<Role> ans = roleRepository.findAll();
        return ans;
    }
}
