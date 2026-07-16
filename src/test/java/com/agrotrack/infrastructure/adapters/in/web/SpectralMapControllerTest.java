package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.SpectralMapDto;
import com.agrotrack.domain.model.entities.SpectralMap;
import com.agrotrack.domain.model.enums.MapStatus;
import com.agrotrack.domain.model.enums.SpectralMapType;
import com.agrotrack.domain.port.in.lot.RegisterSpectralMapUseCase;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SpectralMapControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegisterSpectralMapUseCase registerSpectralMapUseCase;

    @InjectMocks
    private SpectralMapController spectralMapController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(spectralMapController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void registerMapSuccess() throws Exception {
        UUID lotId = UUID.randomUUID();
        UUID mapId = UUID.randomUUID();
        LocalDateTime flightDate = LocalDateTime.now();

        SpectralMapDto requestDto = SpectralMapDto.builder()
                .minioRawPath("crudo/test.tif")
                .flightDate(flightDate)
                .indexType(SpectralMapType.NDVI)
                .cloudCoverPercentage(10.0)
                .resolutionGSD(5.0)
                .meanIndexValue(0.7)
                .build();

        com.agrotrack.domain.model.entities.Lot mockLot = org.mockito.Mockito.mock(com.agrotrack.domain.model.entities.Lot.class);
        SpectralMap mockedMap = SpectralMap.create(
                "crudo/test.tif",
                flightDate,
                SpectralMapType.NDVI,
                10.0,
                5.0,
                0.7,
                mockLot
        );
        mockedMap.setIdMap(mapId);
        mockedMap.setMapStatus(MapStatus.PENDING);

        when(registerSpectralMapUseCase.executeRegisterSpectralMap(
                eq("crudo/test.tif"),
                any(LocalDateTime.class),
                eq(SpectralMapType.NDVI),
                eq(10.0),
                eq(5.0),
                eq(0.7),
                eq(lotId)
        )).thenReturn(mockedMap);

        String jsonRequest = "{" +
                "\"minioRawPath\":\"crudo/test.tif\"," +
                "\"flightDate\":\"" + flightDate.toString() + "\"," +
                "\"indexType\":\"NDVI\"," +
                "\"cloudCoverPercentage\":10.0," +
                "\"resolutionGSD\":5.0," +
                "\"meanIndexValue\":0.7" +
                "}";

        mockMvc.perform(post("/api/v1/lots/{lotId}/maps", lotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMap").value(mapId.toString()))
                .andExpect(jsonPath("$.minioRawPath").value("crudo/test.tif"))
                .andExpect(jsonPath("$.indexType").value("NDVI"))
                .andExpect(jsonPath("$.mapStatus").value("PENDING"));
    }
}
