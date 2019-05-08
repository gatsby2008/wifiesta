package com.wifiesta.menus.repository;

import com.wifiesta.menus.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
