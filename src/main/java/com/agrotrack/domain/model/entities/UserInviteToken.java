package com.agrotrack.domain.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInviteToken {
    private UUID id;
    private String token;
    private UUID userId;
    private UUID farmId;
    private LocalDateTime expirationDate;

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now());
    }
}
