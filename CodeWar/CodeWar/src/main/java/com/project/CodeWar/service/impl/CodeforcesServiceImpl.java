package com.project.CodeWar.service.impl;

import com.project.CodeWar.dtos.CfApiResponse;
import com.project.CodeWar.dtos.CfProblemsetResponse;
import com.project.CodeWar.dtos.CfSubmission;
import com.project.CodeWar.dtos.CfSubmissionResponse;
import com.project.CodeWar.dtos.CfUser;
import com.project.CodeWar.entity.User;
import com.project.CodeWar.repository.UserRepository;
import com.project.CodeWar.service.CodeforcesService;
import com.project.CodeWar.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.UUID;

@Service
public class CodeforcesServiceImpl implements CodeforcesService {

    private static final Logger logger = LoggerFactory.getLogger(CodeforcesServiceImpl.class);

    @Autowired
    @Lazy
    private CodeforcesService self;

    private CfProblemsetResponse l1Cache = null;
    private long l1CacheExpiry = 0;
    private static final long L1_TTL = 4 * 60 * 60 * 1000; // 4 hours in milliseconds

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CF_API_URL = "https://codeforces.com/api/user.info?handles=";

    private static final String CF_USER_STATUS_URL = "https://codeforces.com/api/user.status?handle={handle}&from=1&count={count}";

    private static final String CF_PROBLEMSET_URL = "https://codeforces.com/api/problemset.problems";

    @Override
    @CacheEvict(cacheNames = "cfRatings", key = "#userId")
    public String generateVerificationToken(Long userId, String handle) {
        logger.info("Generating verification token for userId: {} with handle: {}", userId, handle);

        User user = userService.getUserEntityById(userId);

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
    @CacheEvict(cacheNames = "cfRatings", key = "#userId")
    public boolean verifyHandle(Long userId) {
        logger.info("Starting verification for userId: {}", userId);

        User user = userService.getUserEntityById(userId);

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
        String lastName = response.getResult().get(0).getLastName();
        logger.info("CF API returned firstName: {}, lastName: {} for handle: {}", firstName, lastName, handle);

        String cleanToken = token.trim().toLowerCase();
        boolean match = (firstName != null && firstName.trim().toLowerCase().contains(cleanToken))
                || (lastName != null && lastName.trim().toLowerCase().contains(cleanToken));

        if (match) {
            logger.info("Token match found! Marking userId: {} as verified", userId);
            user.setCodeforcesVerified(true);
            user.setCodeforcesVerificationToken(null);
            userRepository.save(user);
            logger.info("userId: {} successfully linked to CF handle: {}", userId, handle);
            return true;
        }

        logger.warn("Token mismatch for userId: {} — expected: {} in firstName: {} or lastName: {}", userId, token, firstName, lastName);
        return false;
    }

    @Override
    @CacheEvict(cacheNames = "cfRatings", key = "#userId")
    public void unlinkHandle(Long userId) {
        logger.info("Unlinking CF handle for userId: {}", userId);

        User user = userService.getUserEntityById(userId);

        user.setCodeforcesHandle(null);
        user.setCodeforcesVerified(false);
        user.setCodeforcesVerificationToken(null);
        userRepository.save(user);

        logger.info("CF handle unlinked successfully for userId: {}", userId);
    }

    @Override
    public CfUser getUserRating(String handle) {
        logger.info("Fetching rating for handle: {}", handle);

        CfApiResponse response = restTemplate.getForObject(CF_API_URL + handle, CfApiResponse.class);

        if (response == null || !"OK".equals(response.getStatus()) || response.getResult().isEmpty()) {
            logger.error("CF API returned invalid response for handle: {}", handle);
            throw new RuntimeException("Could not fetch Codeforces profile for handle: " + handle);
        }

        CfUser cfUser = response.getResult().get(0);
        logger.info("Fetched rating for handle: {} — rating: {}, rank: {}", handle, cfUser.getRating(), cfUser.getRank());
        return cfUser;
    }

    @Override
    @Cacheable(cacheNames = "cfRatings", key = "#userId")
    public CfUser getUserRatingByUserId(Long userId) {
        logger.info("Fetching CF rating for userId: {}", userId);

        User user = userService.getUserEntityById(userId);

        if (user.getCodeforcesHandle() == null || !user.isCodeforcesVerified()) {
            logger.warn("User {} has no verified CF handle", userId);
            throw new RuntimeException("User has no verified Codeforces handle");
        }

        logger.info("Found verified handle: {} for userId: {}", user.getCodeforcesHandle(), userId);
        return getUserRating(user.getCodeforcesHandle());
    }

    @Override
    public List<CfSubmission> getRecentSubmissions(String handle, int count) {
        logger.info("Fetching last {} submissions for handle: {}", count, handle);

        CfSubmissionResponse response = restTemplate.getForObject(
                CF_USER_STATUS_URL,
                CfSubmissionResponse.class,
                handle, count
        );

        if (response == null || !"OK".equals(response.getStatus())) {
            logger.error("CF API returned invalid response for handle: {}", handle);
            throw new RuntimeException("Could not fetch submissions for handle: " + handle);
        }

        logger.info("Fetched {} submissions for handle: {}", response.getResult().size(), handle);
        return response.getResult();
    }

    @Override
    public CfProblemsetResponse getProblemset() {
        long now = System.currentTimeMillis();
        if (l1Cache != null && now < l1CacheExpiry) {
            logger.info("Returning CF problemset from L1 cache (in-memory)");
            return l1Cache;
        }

        logger.info("L1 cache miss or expired. Fetching CF problemset from Redis/API...");
        CfProblemsetResponse response = self.getProblemsetFromRedis();
        if (response != null && "OK".equals(response.getStatus())) {
            l1Cache = response;
            l1CacheExpiry = now + L1_TTL;
        }
        return response;
    }

    @Override
    @Cacheable(cacheNames = "cfProblemset", key = "'all'")
    public CfProblemsetResponse getProblemsetFromRedis() {
        logger.info("Hitting CF problemset API (Redis cache miss)");
        return restTemplate.getForObject(CF_PROBLEMSET_URL, CfProblemsetResponse.class);
    }

    @Override
    @CachePut(cacheNames = "cfProblemset", key = "'all'")
    public CfProblemsetResponse updateProblemsetInRedis(CfProblemsetResponse response) {
        logger.info("Updating CF problemset in Redis cache");
        return response;
    }

    @Scheduled(fixedRate = 14400000, initialDelay = 1000)
    public void refreshProblemsetCache() {
        logger.info("Scheduled task: refreshing Codeforces problemset cache...");
        try {
            CfProblemsetResponse response = restTemplate.getForObject(CF_PROBLEMSET_URL, CfProblemsetResponse.class);
            if (response != null && "OK".equals(response.getStatus())) {
                self.updateProblemsetInRedis(response);
                l1Cache = response;
                l1CacheExpiry = System.currentTimeMillis() + L1_TTL;
                logger.info("CF problemset cache refreshed successfully in background");
            } else {
                logger.warn("Failed to refresh CF problemset: invalid response status");
            }
        } catch (Exception e) {
            logger.error("Error refreshing CF problemset cache: {}", e.getMessage(), e);
        }
    }
}
