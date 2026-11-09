package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.application.mapper.ApplicationDtoMapper;
import com.agrotrack.domain.exception.BusinessRuleViolationsException;
import com.agrotrack.domain.port.in.alert.CreateAlertUseCase;
import com.agrotrack.domain.port.in.alert.DeleteAlertUseCase;
import com.agrotrack.domain.port.in.alert.GetAlertByIdUseCase;
import com.agrotrack.domain.port.in.alert.GetAlertsByFarmUseCase;
import com.agrotrack.domain.port.in.alert.UploadAlertImageUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateAlertUseCase createAlertUseCase;
    @Mock
    private GetAlertsByFarmUseCase getAlertsByFarmUseCase;
    @Mock
    private GetAlertByIdUseCase getAlertByIdUseCase;
    @Mock
    private DeleteAlertUseCase deleteAlertUseCase;
    @Mock
    private UploadAlertImageUseCase uploadAlertImageUseCase;
    @Mock
    private ApplicationDtoMapper mapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AlertController alertController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadAlertImageSuccessReturnsRelativePath() throws Exception {
        UUID farmId = UUID.randomUUID();
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile file = new MockMultipartFile("file", "evidencia.jpg", "image/jpeg", content);

        String expectedPath = "novedades/" + farmId + "/550e8400-e29b-41d4-a716-446655440000.jpg";
        when(uploadAlertImageUseCase.executeUploadAlertImage(
                eq(farmId), eq("evidencia.jpg"), eq("image/jpeg"), eq((long) content.length), any(InputStream.class)
        )).thenReturn(expectedPath);

        mockMvc.perform(multipart("/api/v1/alerts/images")
                        .file(file)
                        .param("farmId", farmId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value(expectedPath));
    }

    @Test
    void uploadAlertImageEmptyReturnsBadRequest() throws Exception {
        UUID farmId = UUID.randomUUID();
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/v1/alerts/images")
                        .file(emptyFile)
                        .param("farmId", farmId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(uploadAlertImageUseCase, never()).executeUploadAlertImage(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void uploadAlertImageInvalidFormatReturnsBadRequest() throws Exception {
        UUID farmId = UUID.randomUUID();
        MockMultipartFile invalidFile = new MockMultipartFile("file", "script.html", "text/html", "<h1>bad</h1>".getBytes());

        when(uploadAlertImageUseCase.executeUploadAlertImage(
                eq(farmId), eq("script.html"), eq("text/html"), any(Long.class), any(InputStream.class)
        )).thenThrow(new BusinessRuleViolationsException("Formato no permitido"));

        mockMvc.perform(multipart("/api/v1/alerts/images")
                        .file(invalidFile)
                        .param("farmId", farmId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Formato no permitido"));
    }
}
