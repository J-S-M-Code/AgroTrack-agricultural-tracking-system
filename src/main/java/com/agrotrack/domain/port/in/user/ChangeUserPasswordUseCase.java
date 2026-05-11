package com.agrotrack.domain.port.in.user;

public interface ChangeUserPasswordUseCase {
    void execute(String email, String currentRawPassword, String newRawPassword);
}
