package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.domain.port.in.user.GetPersonnelByFarmUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetPersonnelByFarmUseCase getPersonnelByFarmUseCase;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testGetPersonnelByFarm_ShouldReturn200() throws Exception {
        when(getPersonnelByFarmUseCase.executeGetPersonnelByFarm(any(UUID.class))).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/v1/users/farm/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
