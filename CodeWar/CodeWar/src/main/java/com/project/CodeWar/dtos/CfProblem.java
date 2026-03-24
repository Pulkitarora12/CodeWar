package com.project.CodeWar.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CfProblem {
    private Integer contestId;
    private String index;
    private String name;
    private Integer rating;
}