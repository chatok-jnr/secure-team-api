package com.chatokjunior.secureteamapi.user.dto;

import com.chatokjunior.secureteamapi.user.entity.User;
import java.util.*;

import lombok.Builder;
import lombok.Getter;

@Builder 
@Getter 
public class GetAllUserResponse {

    List<User> users;
}
