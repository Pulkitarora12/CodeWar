package com.project.CodeWar.service;

import com.project.CodeWar.dtos.CfUser;

public interface CodeforcesService {
    String generateVerificationToken(Long userId, String handle);
    boolean verifyHandle(Long userId);
    void unlinkHandle(Long userId);
    CfUser getUserRating(String handle);
    CfUser getUserRatingByUserId(Long userId);
}