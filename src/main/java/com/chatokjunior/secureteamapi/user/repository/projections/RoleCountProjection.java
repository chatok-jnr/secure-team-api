package com.chatokjunior.secureteamapi.user.repository.projections;

import com.chatokjunior.secureteamapi.user.entity.Role;

public interface RoleCountProjection {
    Role getRole();
    Long getCount();
}
