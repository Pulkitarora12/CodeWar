package com.project.CodeWar.service;

public interface EmailService {

    void sendResetEmail(String to, String resetUrl);
}
