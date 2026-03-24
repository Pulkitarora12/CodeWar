package com.project.CodeWar.service;

public interface CodeforcesService {
    String generateVerificationToken(Long userId, String handle);
    boolean verifyHandle(Long userId);
    void unlinkHandle(Long userId);
}