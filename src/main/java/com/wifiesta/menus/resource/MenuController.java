package com.wifiesta.menus.resource;

import com.wifiesta.menus.domain.Item;
import com.wifiesta.menus.domain.Menu;
import com.wifiesta.menus.repository.MenuRepository;
import com.wifiesta.menus.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api")
public class MenuController {
    Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private MenuService menuService;

    @GetMapping(value = "/menus")
    public ResponseEntity<List<MenuDTO>> getListOfMenu() {
        long millis_startTime = System.currentTimeMillis();
        List<MenuDTO> allMenus = menuService.getAllMenusDTO();
        long millis_endTime = System.currentTimeMillis();
        log.info("Time taken in milli seconds: " + (millis_endTime - millis_startTime));
        if (!allMenus.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(allMenus);
        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping(value = "/menus/{id}")
    public ResponseEntity<Menu> getMenuById(@PathVariable("id") Long id) {
        long millis_startTime = System.currentTimeMillis();
        Menu menu = menuService.getMenu(id);
        long millis_endTime = System.currentTimeMillis();
        log.info("Time taken in milli seconds: " + (millis_endTime - millis_startTime));
        if (menu != null) return ResponseEntity.status(HttpStatus.OK).body(menu);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping(value = "/menus/{id}/items")
    public ResponseEntity<Map<?, List<Item>>> getItemsForMenu(@PathVariable("id") Long id,
                                                              @RequestParam(value = "groupBy", required = true)
                                                                      String groupBy) {
        long millis_startTime = System.currentTimeMillis();
        Map<?, List<Item>> itemsByPrice = new HashMap<>();

        if (groupBy.compareToIgnoreCase("price") == 0) {
            itemsByPrice = menuService.getItemsForMenuGrupedByPrice(id);
        } else if (groupBy.compareToIgnoreCase("ranking") == 0) {
            itemsByPrice = menuService.getItemsForMenuGrupedByRanking(id);
        }
        long millis_endTime = System.currentTimeMillis();
        log.info("Time taken in milli seconds: " + (millis_endTime - millis_startTime));
        if (itemsByPrice != null) return ResponseEntity.status(HttpStatus.OK).body(itemsByPrice);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

}
