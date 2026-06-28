package com.project.CodeWar.dtos;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CfProblem implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer contestId;
    private String index;
    private String name;
    private Integer rating;
}