package com.wifiesta.menus.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Item {
    @Id
    private Long id;
    private String name;
    private String description;
    @Min(value = 1, message = "Ranking should not be less than 1")
    @Max(value = 5, message = "Ranking should not be greater than 5")
    private int ranking;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime fromTime;
    private LocalTime toTime;
    private int availableDays;
    private double price;
    private String currency;

}
