package com.project.CodeWar.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CfProblemsetResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String status;
    private Result result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<CfProblem> problems;
    }
}