package com.agrotrack.infrastructure.config;

import com.agrotrack.infrastructure.security.JwtChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final List<String> allowedOrigins;

    public WebSocketConfig(
            JwtChannelInterceptor jwtChannelInterceptor,
            @Value("${cors.allowed-origins:http://localhost:4200,http://localhost:8100,http://localhost,https://localhost,capacitor://localhost,https://agrotrackapp.com.ar,https://www.agrotrackapp.com.ar,https://agrotrack.azurewebsites.net}") List<String> allowedOrigins) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitamos un broker en memoria para enviar mensajes al cliente
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijo para mensajes enviados desde el cliente al servidor (opcional por ahora)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint al que se conectará el cliente Angular Web e Ionic/Capacitor Móvil
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost*",
                        "https://localhost*",
                        "capacitor://*",
                        "ionic://*",
                        "http://192.168.*:*",
                        "http://10.*:*",
                        "http://172.*:*",
                        "https://*.agrotrackapp.com.ar",
                        "https://agrotrackapp.com.ar",
                        "https://agrotrack.azurewebsites.net"
                )
                .withSockJS(); // Soporte para navegadores antiguos / fallback
    }
}
