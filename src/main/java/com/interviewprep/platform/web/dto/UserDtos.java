package com.interviewprep.platform.web.dto;

import com.interviewprep.platform.domain.Role;
import com.interviewprep.platform.domain.User;

import java.util.Set;

public class UserDtos {
    public record UserResponse(Long id, String email, String fullName, Set<Role> roles) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles());
        }
    }
}
