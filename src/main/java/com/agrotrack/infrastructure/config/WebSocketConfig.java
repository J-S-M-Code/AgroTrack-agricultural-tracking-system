package com.agrotrack.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitamos un broker en memoria para enviar mensajes al cliente
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijo para mensajes enviados desde el cliente al servidor (opcional por ahora)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint al que se conectará el cliente Angular
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permitir CORS
                .withSockJS(); // Soporte para navegadores antiguos / fallback
    }
}
