package com.wifiesta.menus.service;


import com.wifiesta.menus.domain.Item;
import com.wifiesta.menus.domain.Menu;
import com.wifiesta.menus.repository.ItemRepository;
import com.wifiesta.menus.repository.MenuRepository;
import com.wifiesta.menus.resource.MenuDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuService {
    Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private ItemRepository itemRepository;
    public List<MenuDTO> getAllMenusDTO() {
        List<MenuDTO> menuDTOList = new ArrayList<>();
        this.getAllMenus().stream().forEach(menu -> {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(menu.getId()).toUri();
            menuDTOList.add(MenuDTO.builder().id(menu.getId()).description(menu.getDescription()).uri(location).build());
        });
        return menuDTOList;
    }
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }
    public Menu getMenu(Long id) {
        Optional<Menu> byId = menuRepository.findById(id);
        if(byId.isPresent()) {
            Menu menu = byId.get();
            printSubMenuDescription(menu);
            return menu;
        }
        return null;
    }

    private void printSubMenuDescription(Menu menu) {
        if(menu==null){
            return;
        }
        if(menu!=null){
            log.info(menu.getDescription());
        }
        menu.getSubmenus().forEach(menu1 -> printSubMenuDescription(menu1));
    }

    public Map<Double, List<Item>> getItemsForMenuGrupedByPrice(Long id){
        Map<Double, List<Item>> itemsGroupedByPrice = new HashMap<Double, List<Item>>();
        Menu menu = this.getMenu(id);
        if(menu!=null) {
            itemsGroupedByPrice = menu.getItems().stream()
                    .collect(Collectors.groupingBy(Item::getPrice));
        }
        return itemsGroupedByPrice;
    }

    public Map<Integer, List<Item>> getItemsForMenuGrupedByRanking(Long id) {
        Map<Integer, List<Item>> itemsGroupedByPrice = new HashMap<>();
        Menu menu = this.getMenu(id);
        if(menu!=null) {
            itemsGroupedByPrice = menu.getItems().stream()
                    .collect(Collectors.groupingBy(Item::getRanking));
        }
        return itemsGroupedByPrice;
    }

    public double getTotalItemsAmount(Long menuId){
        Menu menu = this.getMenu(menuId);
        if(menu!=null) {
            return getTotalItemsAmountHelper(menu);
        }
        return 0.0d;

    }
    public double getTotalItemsAmountHelper(Menu menu){
        if(menu==null) {
            return 0;
        }
        return menu.getItems().stream().mapToDouble(Item::getPrice).sum()
                + menu.getSubmenus().stream().mapToDouble(m->getTotalItemsAmountHelper(m)).sum();
    }

    public int getActiveSubmenus(Long menuId){
        Menu menu = this.getMenu(menuId);
        if(menu!=null) {
            return getActiveSubmenusHelper(menu) + (menu.isActive()?-1:0);
        }
        return 0;

    }
    public int getActiveSubmenusHelper(Menu menu){
        if(menu==null){
            return 0;
        }
        return ((menu.isActive()?1:0) + menu.getSubmenus().stream().mapToInt(m->getActiveSubmenusHelper(m)).sum());

    }
}
