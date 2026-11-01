package com.wifiesta.menus.resource

import com.wifiesta.menus.domain.Menu
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MenuControllerTest extends Specification {

    @Autowired
    TestRestTemplate client

    def "GetListOfMenu"() {
        given: "A set of menus is loaded"

        when: "Get all menus available"
        def entity = client.getForEntity("/api/menus", List)

        then: "The status code should be correct"
        entity.body.size() == 4

    }

    def "Get items of menu gruoup by price"() {
        given: "A set of menus is loaded"

        when: "Get the menu items grouped by price"
        def entity = client.getForEntity("/api/menus/$menuId/items?groupBy=price", Map.class)

        then: "The number of grouped items should be correct"
        entity.body.get(price).size == totalItems

        where: "Menu id is... price is... and total gruped items is..."
        menuId || price  || totalItems
        1      || "50.9" || 2
        1      || "45.2" || 1
    }

    def "Get items of menu gruoup by ranking"() {
        given: "A set of menus is loaded"

        when: "Get the menu items grouped by ranking"
        def entity = client.getForEntity("/api/menus/$menuId/items?groupBy=ranking", Map.class)

        then: "The number of grouped items should be correct"
        entity.body.get(ranking).size == totalItems

        where: "Menu id is... ranking is... and total gruped item is..."
        menuId || ranking || totalItems
        1      || "2"     || 1
        1      || "3"     || 2
    }

    def "Check STATUS CODE for menu id using rest service /api/menus/id"() {
        given: "A set of menus is loaded"

        when: "Get the menu trhough rest service using its id"
        def entity = client.getForEntity("/api/menus/$menuId", Menu)

        then: "The status code should be correct"
        entity.statusCode == result

        where: "Menu id is... result is..."
        menuId || result
        1      || OK
        2      || OK
        3      || OK
        1004   || NOT_FOUND

    }

    // spec: B1, B2, B3, B7
    def "Get the summary of menu #menuId through /api/menus/id/summary"() {
        given: "A set of menus is loaded"

        when: "Get the menu summary through the rest service"
        def entity = client.getForEntity("/api/menus/$menuId/summary", Map)

        then: "The status code and the aggregates should be correct"
        entity.statusCode == OK
        entity.body.menuId == menuId
        entity.body.description == "Menu-$menuId".toString()
        entity.body.totalAmount == totalAmount
        entity.body.currency == null
        entity.body.activeSubmenus == activeSubmenus

        where: "Menu id is... total amount is... active submenus are..."
        menuId || totalAmount || activeSubmenus
        1      || 497.0       || 3
        2      || 100.0       || 0
        3      || 250.0       || 1
        4      || 150.0       || 0
    }

    // spec: B4
    def "Get the summary of an unknown menu returns NOT_FOUND"() {
        when: "Get the summary of a menu that does not exist"
        def entity = client.getForEntity("/api/menus/1004/summary", String)

        then: "The status code should be NOT_FOUND with no body"
        entity.statusCode == NOT_FOUND
        entity.body == null
    }
}
