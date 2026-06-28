package com.project.CodeWar.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CfUser implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String handle;
    private String firstName;
    private Integer rating;
    private String rank;
    private Integer maxRating;
    private String maxRank;
}