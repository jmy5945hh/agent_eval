package com.example.agenteval.domain.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrajectoryEntry {
    private String role;
    private String kind;
    private String time;
    private String title;
    private String content;
}
