package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.dto.FarmDto;
import com.agrotrack.domain.model.entities.Farm;
import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.port.in.farm.CreateFarmUseCase;
import com.agrotrack.domain.port.in.farm.GetFarmsUseCase;
import com.agrotrack.infrastructure.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
class FarmControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateFarmUseCase createFarmUseCase;

    @Mock
    private GetFarmsUseCase getFarmsUseCase;

    @Mock
    private com.agrotrack.application.mapper.ApplicationDtoMapper applicationDtoMapper;

    @InjectMocks
    private FarmController farmController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(farmController)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(CustomUserDetails.class);
                    }
                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter, org.springframework.web.method.support.ModelAndViewContainer mavContainer, org.springframework.web.context.request.NativeWebRequest webRequest, org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                        User mockUserInner = org.mockito.Mockito.mock(User.class);
                        // We use a fixed UUID for testing
                        org.mockito.Mockito.lenient().when(mockUserInner.getIdUser()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                        org.mockito.Mockito.lenient().when(mockUserInner.getRole()).thenReturn(com.agrotrack.domain.model.enums.UserRole.OWNER);
                        return new CustomUserDetails(mockUserInner);
                    }
                })
                .build();
    }

    @Test
    void testGetMyFarms_ShouldReturn200() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        when(getFarmsUseCase.executeGetFarmsByUser(userId)).thenReturn(List.of(FarmDto.builder().idFarm(UUID.randomUUID()).name("Test Farm").build()));

        mockMvc.perform(get("/api/v1/farms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Farm"));
    }

    @Test
    void testCreateFarm_ShouldReturn201() throws Exception {
        FarmDto farmDto = FarmDto.builder().name("New Farm").build();
        Farm mockFarm = org.mockito.Mockito.mock(Farm.class);
        UUID mockId = UUID.randomUUID();
        when(mockFarm.getIdFarm()).thenReturn(mockId);

        when(createFarmUseCase.executeCreateFarm(
                any(), any(), any(), any(),
                any(), any(), any(), org.mockito.ArgumentMatchers.anyDouble(), any()
        )).thenReturn(mockFarm);
        
        when(applicationDtoMapper.toFarmDto(mockFarm)).thenReturn(FarmDto.builder().idFarm(mockId).name("New Farm").build());

        mockMvc.perform(post("/api/v1/farms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(farmDto)))
                .andExpect(status().isCreated());
    }
}
