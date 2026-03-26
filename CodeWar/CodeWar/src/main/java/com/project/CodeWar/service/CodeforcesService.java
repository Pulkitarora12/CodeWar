package com.project.CodeWar.service;

import com.project.CodeWar.dtos.CfSubmission;
import com.project.CodeWar.dtos.CfUser;

import java.util.List;

public interface CodeforcesService {
    String generateVerificationToken(Long userId, String handle);
    boolean verifyHandle(Long userId);
    void unlinkHandle(Long userId);
    CfUser getUserRating(String handle);
    CfUser getUserRatingByUserId(Long userId);
    List<CfSubmission> getRecentSubmissions(String handle, int count);
}