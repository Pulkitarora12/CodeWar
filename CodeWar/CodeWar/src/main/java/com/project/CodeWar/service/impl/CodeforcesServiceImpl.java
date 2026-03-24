package com.project.CodeWar.service.impl;

import com.project.CodeWar.dtos.CfApiResponse;
import com.project.CodeWar.entity.User;
import com.project.CodeWar.repository.UserRepository;
import com.project.CodeWar.service.CodeforcesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class CodeforcesServiceImpl implements CodeforcesService {

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CF_API_URL = "https://codeforces.com/api/user.info?handles=";

    @Override
    public String generateVerificationToken(Long userId, String handle) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = "cw-" + UUID.randomUUID().toString().substring(0, 8);

        user.setCodeforcesHandle(handle);
        user.setCodeforcesVerified(false);
        user.setCodeforcesVerificationToken(token);
        userRepository.save(user);

        return token;
    }

    @Override
    public boolean verifyHandle(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String handle = user.getCodeforcesHandle();
        String token = user.getCodeforcesVerificationToken();

        if (handle == null || token == null) {
            throw new RuntimeException("Generate a token first");
        }

        CfApiResponse response = restTemplate.getForObject(CF_API_URL + handle, CfApiResponse.class);

        if (response == null || !"OK".equals(response.getStatus()) || response.getResult().isEmpty()) {
            throw new RuntimeException("Could not fetch Codeforces profile, check your handle");
        }

        String firstName = response.getResult().get(0).getFirstName();

        if (firstName != null && firstName.contains(token)) {
            user.setCodeforcesVerified(true);
            user.setCodeforcesVerificationToken(null); // clear token after success
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void unlinkHandle(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setCodeforcesHandle(null);
        user.setCodeforcesVerified(false);
        user.setCodeforcesVerificationToken(null);
        userRepository.save(user);
    }
}