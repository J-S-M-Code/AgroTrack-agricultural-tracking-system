package com.agrotrack.infrastructure.adapters.in.web;

import com.agrotrack.domain.model.entities.Password;
import com.agrotrack.domain.model.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.agrotrack.infrastructure.security.CustomUserDetails;
import java.util.Collections;

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
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().isAssignableFrom(CustomUserDetails.class);
                }
                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                    User mockUser = User.create("Test", "User", "123", "456", "Dir", "test@test.com", new com.agrotrack.domain.model.entities.Password("SecurePass1!"));
                    return new CustomUserDetails(mockUser);
                }
            })
            .build();
    }

    @Test
    void testGetPersonnelByFarm_ShouldReturn200() throws Exception {
        when(getPersonnelByFarmUseCase.executeGetPersonnelByFarm(any(UUID.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/users/farm/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }
}
