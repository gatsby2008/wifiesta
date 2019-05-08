package com.wifiesta.menus.service

import com.wifiesta.menus.service.MenuService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Unroll

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MenuServiceTest extends Specification {
    @Autowired
    MenuService menuService

    def "Check all menus available"() {
        given:"A set of menus is loaded"

        when:"get all menus"
        def menus = menuService.getAllMenus()

        then:"the sum of all prices of all items should be correct"
        menus.size() == 4
    }

    def "Check description that belong to a menu by id"() {
        given:"A set of menus is loaded"

        when:"Get menu by id"
        def menu = menuService.getMenu(menuId)

        then:"The sum of all prices of all items should be correct"
        menu.description == result

        where: "Menu id is... Description is"
        menuId || result
            1  ||  "Menu-1"
            2  ||  "Menu-2"
            3  ||  "Menu-3"
    }

    def "Check the items form a menu are gruoped by price"() {
        given:"A set of menus is loaded"

        when:"Get items group by price"
        def menus = menuService.getItemsForMenuGrupedByPrice(menuId)

        then:"For each price should be a quantity of items"
        menus.get(price).size() == totalOfItems

        where: "id is, price, reult"
        menuId  || price || totalOfItems
            1L  || 50.9d || 2
            1L  || 45.2d || 1
    }

    def "Check the items form a menu are gruoped by ranking"() {
        given:"A set of menus is loaded"

        when:"Get items group by ranking"
        def menus = menuService.getItemsForMenuGrupedByRanking(menuId)

        then:"For each price should be a quantity of items"
        menus.get(ranking).size() == totalOfItems

        where: "id is, ranking, reult"
        menuId  || ranking   || totalOfItems
            1L  ||      2    || 1
            1L  ||      3    || 2
    }
    @Unroll
    def "Check SUM OF ALL PRICE OF ALL ITEMS by menu id"() {
        given:"A set of menus is loaded"

        when:"Sum price of all items"
        def total = menuService.getTotalItemsAmount(menuId)

        then:"the sum of all prices of all items should be correct"
        total == result

        where:"Menu id is... result is..."
        menuId || result
            1  || 497.0
            2  || 100
            3  || 250
    }

    @Unroll
    def "Check QUANTITY OF ACTIVE SUBMENU by menu id"() {
        given:"A set of menus is loaded"

        when:"Sum total of active submenus"
        def total = menuService.getActiveSubmenus(menuId)

        then:"the quantity of active submenus should be correct"
        total == result

        where:"Menu id is... result is..."
        menuId || result
             1 || 3
             2 || 0
             3 || 1
    }
}
