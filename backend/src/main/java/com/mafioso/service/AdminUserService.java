package com.mafioso.service;

import com.mafioso.dto.ConsultantSummaryResponse;
import com.mafioso.dto.CreateConsultantRequest;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.UserResponse;

import java.sql.SQLException;
import java.util.Map;

/**
 * Administrative user management contract for dashboard metrics, students, and consultants.
 */
public interface AdminUserService {

    Map<String, Object> getAdminDashboard(long adminUserId) throws SQLException;

    PagedResponse<UserResponse> listStudents(String search, String status, int page, int pageSize) throws SQLException;

    Map<String, Object> getStudentDetails(long studentId) throws SQLException;

    UserResponse updateStudentStatus(long studentId, String rawStatus, long actingAdminId) throws SQLException;

    PagedResponse<ConsultantSummaryResponse> listConsultants(String search, String status, int page, int pageSize) throws SQLException;

    Map<String, Object> getConsultantDetails(long consultantId) throws SQLException;

    ConsultantSummaryResponse createConsultant(CreateConsultantRequest request) throws SQLException;

    ConsultantSummaryResponse updateConsultantStatus(long consultantId, String rawStatus, long actingAdminId) throws SQLException;
}
