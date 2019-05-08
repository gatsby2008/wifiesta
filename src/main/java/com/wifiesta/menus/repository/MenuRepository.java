package com.wifiesta.menus.repository;

import com.wifiesta.menus.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByDescription(String description);
}
