package com.chatokjunior.secureteamapi.user.entity;

import com.chatokjunior.secureteamapi.security.Permission;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum Role {
    ADMIN(Set.of(
            Permission.USER_READ,
            Permission.USER_UPDATE,
            Permission.USER_DELETE,

            Permission.PROJECT_CREATE,
            Permission.PROJECT_UPDATE,
            Permission.PROJECT_DELETE
    )),
    MANAGER(Set.of(
            Permission.USER_READ,
            Permission.PROJECT_CREATE,
            Permission.PROJECT_READ,
            Permission.PROJECT_UPDATE
    )),
    EMPLOYEE(Set.of(
            Permission.PROJECT_READ
    ));

    private final Set<Permission> permissions;
}
