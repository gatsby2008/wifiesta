package com.wifiesta.menus.domain;

import lombok.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Menu {
    @Id
    private long id;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Item> items;
    private String description;
    private boolean isActive;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Menu> submenus;
    private TypeMenu type;
    private String pictureName;
}
