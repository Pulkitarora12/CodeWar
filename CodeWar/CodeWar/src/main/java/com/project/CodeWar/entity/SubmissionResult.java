package com.project.CodeWar.entity;

public enum SubmissionResult {
    AC,                     // OK
    WRONG_ANSWER,           // WRONG_ANSWER
    TIME_LIMIT_EXCEEDED,    // TIME_LIMIT_EXCEEDED
    MEMORY_LIMIT_EXCEEDED,  // MEMORY_LIMIT_EXCEEDED
    RUNTIME_ERROR,          // RUNTIME_ERROR
    COMPILATION_ERROR,      // COMPILATION_ERROR
    IDLENESS_LIMIT_EXCEEDED,// IDLENESS_LIMIT_EXCEEDED
    PARTIAL,                // PARTIAL
    SKIPPED,                // SKIPPED / CHALLENGED
    TESTING,                // verdict field absent or TESTING
    OTHER                   // FAILED, CRASHED, SECURITY_VIOLATED, etc.
}