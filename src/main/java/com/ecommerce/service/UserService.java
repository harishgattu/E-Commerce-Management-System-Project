package com.ecommerce.service;

import com.ecommerce.dto.AuthRequest;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.entity.User;

public interface UserService {
    AuthResponse registerUser(RegisterRequest request);
    AuthResponse authenticateUser(AuthRequest request);
    User getUserById(Long id);
    User getUserByUsernameOrEmail(String identifier);
}
