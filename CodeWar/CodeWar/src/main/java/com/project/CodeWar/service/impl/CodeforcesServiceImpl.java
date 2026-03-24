package com.project.CodeWar.service.impl;

import com.project.CodeWar.dtos.CfApiResponse;
import com.project.CodeWar.entity.User;
import com.project.CodeWar.repository.UserRepository;
import com.project.CodeWar.service.CodeforcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class CodeforcesServiceImpl implements CodeforcesService {

    private static final Logger logger = LoggerFactory.getLogger(CodeforcesServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CF_API_URL = "https://codeforces.com/api/user.info?handles=";

    @Override
    public String generateVerificationToken(Long userId, String handle) {
        logger.info("Generating verification token for userId: {} with handle: {}", userId, handle);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = "cw-" + UUID.randomUUID().toString().substring(0, 8);
        logger.info("Generated token: {} for handle: {}", token, handle);

        user.setCodeforcesHandle(handle);
        user.setCodeforcesVerified(false);
        user.setCodeforcesVerificationToken(token);
        userRepository.save(user);

        logger.info("Token saved to DB for userId: {}", userId);
        return token;
    }

    @Override
    public boolean verifyHandle(Long userId) {
        logger.info("Starting verification for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String handle = user.getCodeforcesHandle();
        String token = user.getCodeforcesVerificationToken();

        logger.info("Fetched from DB — handle: {}, token: {}", handle, token);

        if (handle == null || token == null) {
            logger.warn("Handle or token is null for userId: {}", userId);
            throw new RuntimeException("Generate a token first");
        }

        logger.info("Hitting CF API for handle: {}", handle);
        CfApiResponse response = restTemplate.getForObject(CF_API_URL + handle, CfApiResponse.class);

        if (response == null || !"OK".equals(response.getStatus()) || response.getResult().isEmpty()) {
            logger.error("CF API returned invalid response for handle: {}", handle);
            throw new RuntimeException("Could not fetch Codeforces profile, check your handle");
        }

        String firstName = response.getResult().get(0).getFirstName();
        logger.info("CF API returned firstName: {} for handle: {}", firstName, handle);

        if (firstName != null && firstName.contains(token)) {
            logger.info("Token match found! Marking userId: {} as verified", userId);
            user.setCodeforcesVerified(true);
            user.setCodeforcesVerificationToken(null);
            userRepository.save(user);
            logger.info("userId: {} successfully linked to CF handle: {}", userId, handle);
            return true;
        }

        logger.warn("Token mismatch for userId: {} — expected: {} in firstName: {}", userId, token, firstName);
        return false;
    }

    @Override
    public void unlinkHandle(Long userId) {
        logger.info("Unlinking CF handle for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setCodeforcesHandle(null);
        user.setCodeforcesVerified(false);
        user.setCodeforcesVerificationToken(null);
        userRepository.save(user);

        logger.info("CF handle unlinked successfully for userId: {}", userId);
    }
}