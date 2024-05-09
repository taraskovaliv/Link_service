package dev.kovaliv.data.dto;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatisticDto {

    private String name;

    private String description;

    private Long count;
}
