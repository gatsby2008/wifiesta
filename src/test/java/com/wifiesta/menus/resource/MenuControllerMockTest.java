package com.wifiesta.menus.resource;

import com.wifiesta.menus.domain.Menu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class MenuControllerMockTest {
    private Menu mockMenu;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getListOfMenu_MenusExist_Ok_1() throws Exception {
        this.mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Menu-1"))
                .andExpect(jsonPath("$[1].description").value("Menu-2"))
                .andExpect(jsonPath("$[2].description").value("Menu-3"))
                .andExpect(jsonPath("$[3].description").value("Menu-4"));

    }

    @Test
    public void getMenuById_theMenuExist_Ok_1() throws Exception {


        this.mockMvc.perform(get("/api/menus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submenus[0].description").value("Menu-2"))
                .andExpect(jsonPath("$.submenus[1].description").value("Menu-3"))
                .andExpect(jsonPath("$.submenus[1].submenus[0].description").value("Menu-4"));
    }

    @Test
    public void getMenuById_MenuNotExist_Ok_1() throws Exception {


        this.mockMvc.perform(get("/api/menus/100"))
                .andExpect(status().isNotFound());
    }

    // spec: B1, B2, B3, B7
    @Test
    public void getMenuSummary_theMenuExist_Ok_1() throws Exception {
        this.mockMvc.perform(get("/api/menus/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.menuId").value(1))
                .andExpect(jsonPath("$.description").value("Menu-1"))
                .andExpect(jsonPath("$.totalAmount").value(497.0))
                .andExpect(jsonPath("$.currency").value(nullValue()))
                .andExpect(jsonPath("$.activeSubmenus").value(3));
    }

    // spec: B4
    @Test
    public void getMenuSummary_MenuNotExist_NotFound_1() throws Exception {
        this.mockMvc.perform(get("/api/menus/100/summary"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }
}
