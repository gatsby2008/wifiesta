package com.wifiesta.menus.resource;

import lombok.Builder;
import lombok.Data;

import java.net.URI;
@Data
@Builder
public class MenuDTO {
    private long id;
    private String description;
    private URI uri;

}
