package com.wifiesta.menus.resource;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuSummaryDTO {
    private long menuId;
    private String description;
    private Double totalAmount;
    private String currency;
    private int activeSubmenus;

}
