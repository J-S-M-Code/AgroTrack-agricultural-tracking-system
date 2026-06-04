package com.agrotrack.infrastructure.adapters.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FarmControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private FarmController farmController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(farmController).build();
    }

    @Test
    void testGetAllFarms_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/farms"))
                .andExpect(status().isOk());
    }
}
