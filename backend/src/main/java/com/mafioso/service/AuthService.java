package com.mafioso.service;

import com.mafioso.dto.LoginRequest;
import com.mafioso.dto.RegisterRequest;
import com.mafioso.dto.UserResponse;
import com.mafioso.model.Role;
import com.mafioso.model.User;

import java.sql.SQLException;

/**
 * Authentication and user identity business contract.
 */
public interface AuthService {

    UserResponse registerStudent(RegisterRequest request) throws SQLException;

    User authenticate(LoginRequest request, String clientIp) throws SQLException;

    UserResponse getActiveUserProfile(long userId) throws SQLException;

    String resolveDashboardRoute(Role role);
}
