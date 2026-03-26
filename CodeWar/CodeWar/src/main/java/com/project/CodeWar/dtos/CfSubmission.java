package com.project.CodeWar.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CfSubmission {

    private Long id;
    private Long creationTimeSeconds;
    private String verdict;
    private CfProblem problem;
}