package com.project.CodeWar.service;

import com.project.CodeWar.entity.Submission;

public interface SubmissionService {
    Submission checkAndRecordSubmission(String roomCode);
}