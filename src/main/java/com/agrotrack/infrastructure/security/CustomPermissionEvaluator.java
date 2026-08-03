package com.agrotrack.infrastructure.security;

import com.agrotrack.domain.model.entities.User;
import com.agrotrack.domain.model.enums.UserRole;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if ((authentication == null) || (targetDomainObject == null) || !(permission instanceof String)) {
            return false;
        }

        String requiredRole = (String) permission;
        String farmIdStr = targetDomainObject.toString();

        return checkFarmRole(authentication, farmIdStr, requiredRole);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if ((authentication == null) || (targetId == null) || !(permission instanceof String)) {
            return false;
        }
        return hasPermission(authentication, targetId, permission);
    }

    private boolean checkFarmRole(Authentication auth, String farmIdStr, String requiredRoleStr) {
        if (!(auth.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();
        UUID farmId;
        try {
            farmId = UUID.fromString(farmIdStr);
        } catch (IllegalArgumentException e) {
            return false;
        }

        Optional<UserRole> optionalRole = user.getRoleForFarm(farmId);
        if (optionalRole.isEmpty()) {
            return false;
        }

        UserRole userRoleInFarm = optionalRole.get();
        if ("ANY".equals(requiredRoleStr)) {
            return true; // The user has at least some role on the farm (checked above)
        }

        UserRole requiredRole;
        try {
            requiredRole = UserRole.valueOf(requiredRoleStr);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Jerarquía de roles: OWNER y FOREMAN son especiales
        if (userRoleInFarm == UserRole.OWNER) {
            return true;
        }

        if (userRoleInFarm == UserRole.FOREMAN && requiredRole != UserRole.OWNER && requiredRole != UserRole.AGRONOMIST) {
            return true;
        }

        return userRoleInFarm == requiredRole;
    }
}
