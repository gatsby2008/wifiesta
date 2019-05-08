package com.wifiesta.menus;

import com.wifiesta.menus.domain.Item;
import com.wifiesta.menus.domain.Menu;
import com.wifiesta.menus.domain.TypeMenu;
import com.wifiesta.menus.repository.ItemRepository;
import com.wifiesta.menus.repository.MenuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class Startup {
    Logger log = LoggerFactory.getLogger(getClass());

    private MenuRepository menuRepository;
    private ItemRepository itemRepository;

    @Autowired
    public Startup(MenuRepository menuRepository, ItemRepository itemRepository) {
        this.menuRepository = menuRepository;
        this.itemRepository = itemRepository;
    }

    @Configuration
    @Profile({"development", "test"})
    class LocalDevelopmentVersion {
        @PostConstruct
        public void populateData(){
            ArrayList<Menu> menus = LoadDefaultMenus();
            menus.stream().forEach(it -> createMenuDoesntExist(it));
        }
    }
    private ArrayList<Menu> LoadDefaultMenus() {
        ArrayList<Menu> menuArrayList = new ArrayList<>();
        Menu menu = Menu.builder()
                .id(1L)
                .isActive(true)
                .description("Menu-1")
                .type(TypeMenu.DAILY_MENU)
                .isActive(true)
                .build();
        List<Item> itemList = new ArrayList<>();
        Item item = Item.builder()
                .id(1L)
                .description("Item-1-Description")
                .name("Item-1")
                .ranking(3)
                .price(45.20)
                .build();
        Item item2 = Item.builder()
                .id(2L)
                .description("Item-2-Description")
                .name("Item-2")
                .ranking(2)
                .price(50.90)
                .build();
        Item item3 = Item.builder()
                .id(3L)
                .description("Item-3-Description")
                .name("Item-3")
                .ranking(3)
                .price(50.90)
                .build();
        itemList.add(item);
        itemList.add(item2);
        itemList.add(item3);

        Menu subMenu1 = Menu.builder()
                .id(2L)
                .isActive(true)
                .description("Menu-2")
                .type(TypeMenu.DAILY_MENU)
                .isActive(true)
                .build();
        List<Item> itemList1 = Arrays.asList(
                Item.builder()
                        .id(4L)
                        .description("Item-4-Description")
                        .name("Item-4")
                        .ranking(4)
                        .price(100.00)
                        .build());
        subMenu1.setItems(itemList1);

        Menu subMenu2 = Menu.builder()
                .id(3L)
                .isActive(true)
                .description("Menu-3")
                .type(TypeMenu.DAILY_MENU)
                .isActive(true)
                .build();
        subMenu2.setItems(Arrays.asList(Item.builder()
                .id(5L)
                .description("Item-5-Description")
                .name("Item-5")
                .ranking(2)
                .price(100.00)
                .build()));
        subMenu2.setSubmenus(Arrays.asList(Menu.builder()
                .id(4L)
                .isActive(true)
                .description("Menu-4")
                .type(TypeMenu.DAILY_MENU)
                .isActive(true)
                .items(Arrays.asList(Item.builder()
                        .id(6L)
                        .description("Item-6-Description")
                        .name("Item-6")
                        .ranking(2)
                        .price(150.00)
                        .build()))
                .build()));

        List<Menu> subMenuList = Arrays.asList(subMenu1, subMenu2);

        menu.setItems(itemList);
        menu.setSubmenus(subMenuList);

        menuArrayList.add(menu);

        return menuArrayList;
    }
    private void createMenuDoesntExist(Menu menu) {
        Optional<Menu> existing = menuRepository.findByDescription(menu.getDescription());
        if (!existing.isPresent()){
            log.info("Creating record for " + menu.getDescription());
            this.menuRepository.save(menu);
        }
    }

}
