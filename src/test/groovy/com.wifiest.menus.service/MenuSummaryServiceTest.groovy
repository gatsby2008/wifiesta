package com.wifiesta.menus.service

import com.wifiesta.menus.domain.Item
import com.wifiesta.menus.domain.Menu
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Pure unit tests for MenuService.buildSummary on in-memory trees.
 * No Spring context and nothing is persisted, so the shared test DB is untouched.
 */
class MenuSummaryServiceTest extends Specification {

    MenuService menuService = new MenuService()

    private static Item item(double price, String currency = null) {
        Item.builder().price(price).currency(currency).build()
    }

    private static Menu menu(long id, boolean active, List<Item> items = [], List<Menu> submenus = []) {
        Menu.builder().id(id).description("Menu-$id").isActive(active).items(items).submenus(submenus).build()
    }

    // spec: B2, B3
    def "an inactive submenu hides its whole subtree from totalAmount and activeSubmenus"() {
        given: "an active root with one active submenu and one inactive submenu that has an active child"
        def nestedActive = menu(4, true, [item(1000.0)])
        def inactive = menu(3, false, [item(500.0)], [nestedActive])
        def active = menu(2, true, [item(20.0)])
        def root = menu(1, true, [item(10.0)], [active, inactive])

        when:
        def summary = menuService.buildSummary(root)

        then:
        summary.menuId == 1L
        summary.description == "Menu-1"
        summary.totalAmount == 30.0d
        summary.activeSubmenus == 1
    }

    // spec: B2, B3
    def "active submenus are followed at any depth"() {
        given:
        def level3 = menu(4, true, [item(3.0)])
        def level2 = menu(3, true, [item(2.0)], [level3])
        def level1 = menu(2, true, [item(1.0)], [level2])
        def root = menu(1, true, [], [level1])

        when:
        def summary = menuService.buildSummary(root)

        then:
        summary.totalAmount == 6.0d
        summary.activeSubmenus == 3
    }

    // spec: B2, B3
    @Unroll
    def "the root's own items always count and the root is never counted (root active: #rootActive)"() {
        given:
        def root = menu(1, rootActive, [item(7.0)], [menu(2, true, [item(3.0)])])

        when:
        def summary = menuService.buildSummary(root)

        then:
        summary.totalAmount == 10.0d
        summary.activeSubmenus == 1

        where:
        rootActive << [true, false]
    }

    // spec: B6, B7
    @Unroll
    def "currency uniformity: #currencies -> totalAmount #expectedTotal, currency #expectedCurrency"() {
        given:
        def items = currencies.collect { item(10.0, it) }
        def root = menu(1, true, [items[0]], [menu(2, true, items.drop(1))])

        when:
        def summary = menuService.buildSummary(root)

        then:
        summary.totalAmount == expectedTotal
        summary.currency == expectedCurrency
        summary.activeSubmenus == 1

        where:
        currencies          || expectedTotal | expectedCurrency
        ["USD", "USD"]      || 20.0d         | "USD"
        [null, null]        || 20.0d         | null
        ["USD", "EUR"]      || null          | null
        [null, "USD"]       || null          | null
        ["USD", null]       || null          | null
    }

    // spec: B6
    def "items of an inactive submenu do not make the currencies mixed"() {
        given:
        def root = menu(1, true, [item(10.0, "USD")], [menu(2, false, [item(99.0, "EUR")])])

        when:
        def summary = menuService.buildSummary(root)

        then:
        summary.totalAmount == 10.0d
        summary.currency == "USD"
        summary.activeSubmenus == 0
    }

    // spec: B7
    def "a tree with no counted items has totalAmount 0.0 and currency null"() {
        given:
        def root = menu(1, true, [], [menu(2, false, [item(5.0, "USD")])])

        when:
        def summary = menuService.buildSummary(root)

        then:
        summary.totalAmount == 0.0d
        summary.currency == null
        summary.activeSubmenus == 0
    }

    // spec: B2
    def "null item and submenu lists are treated as empty"() {
        given:
        def root = Menu.builder().id(1L).description("Menu-1").isActive(true).build()

        when:
        def summary = menuService.buildSummary(root)

        then:
        summary.totalAmount == 0.0d
        summary.currency == null
        summary.activeSubmenus == 0
    }
}
