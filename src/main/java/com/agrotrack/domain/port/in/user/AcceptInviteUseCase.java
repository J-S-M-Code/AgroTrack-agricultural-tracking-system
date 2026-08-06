package com.agrotrack.domain.port.in.user;

public interface AcceptInviteUseCase {
    void executeAcceptInvite(String token, String newPassword);
}
