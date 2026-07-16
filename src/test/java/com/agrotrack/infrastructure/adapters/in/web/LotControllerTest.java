package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.LotDto;
import com.agrotrack.domain.model.entities.Lot;
import com.agrotrack.domain.port.in.lot.CreateLotUseCase;
import com.agrotrack.domain.port.in.lot.GetLotsByFarmUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class LotControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateLotUseCase createLotUseCase;

    @Mock
    private GetLotsByFarmUseCase getLotsByFarmUseCase;

    @InjectMocks
    private LotController lotController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(lotController).build();
    }

    @Test
    void testGetLotsByFarm_ShouldReturn200() throws Exception {
        UUID farmId = UUID.randomUUID();
        when(getLotsByFarmUseCase.executeGetLotsByFarm(farmId))
                .thenReturn(List.of(LotDto.builder().idLot(UUID.randomUUID()).name("Lot 1").build()));

        mockMvc.perform(get("/api/v1/lots/farm/" + farmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Lot 1"));
    }

    @Test
    void testCreateLot_ShouldReturn201() throws Exception {
        UUID farmId = UUID.randomUUID();
        LotDto lotDto = LotDto.builder().name("New Lot").hectares(10.5).build();

        Lot mockLot = org.mockito.Mockito.mock(Lot.class);
        when(mockLot.getIdLot()).thenReturn(UUID.randomUUID());

        when(createLotUseCase.executeCreateLot(
                any(), any(), org.mockito.ArgumentMatchers.anyDouble(), any(), any(), any(), any()
        )).thenReturn(mockLot);

        mockMvc.perform(post("/api/v1/lots/farm/" + farmId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lotDto)))
                .andExpect(status().isCreated());
    }
}
