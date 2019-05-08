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
}
