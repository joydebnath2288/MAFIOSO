package com.mafioso.service;

import com.mafioso.dto.ApplicationResponse;
import com.mafioso.dto.AssignConsultantRequest;
import com.mafioso.dto.AssignedStudentSummaryResponse;
import com.mafioso.dto.CreateApplicationRequest;
import com.mafioso.dto.PagedResponse;
import com.mafioso.dto.UpdateApplicationStatusRequest;

import java.sql.SQLException;
import java.util.Map;

/**
 * Service contract for student applications, status history, and consultant assignments.
 */
public interface ApplicationService {

    ApplicationResponse createApplication(long studentId, CreateApplicationRequest request) throws SQLException;

    PagedResponse<ApplicationResponse> listStudentApplications(long studentId,
                                                               String status,
                                                               int page,
                                                               int pageSize) throws SQLException;

    Map<String, Object> getStudentApplicationDetail(long studentId, long applicationId) throws SQLException;

    Map<String, Object> getStudentDashboardSummary(long studentId) throws SQLException;

    Map<String, Object> getConsultantDashboard(long consultantId) throws SQLException;

    PagedResponse<AssignedStudentSummaryResponse> listConsultantAssignedStudents(long consultantId,
                                                                               String search,
                                                                               int page,
                                                                               int pageSize) throws SQLException;

    Map<String, Object> getConsultantAssignedStudentDetail(long consultantId, long studentId) throws SQLException;

    PagedResponse<ApplicationResponse> listConsultantApplications(long consultantId,
                                                                  String search,
                                                                  String status,
                                                                  int page,
                                                                  int pageSize) throws SQLException;

    Map<String, Object> getConsultantApplicationDetail(long consultantId, long applicationId) throws SQLException;

    Map<String, Object> updateConsultantApplicationStatus(long consultantId,
                                                          long applicationId,
                                                          UpdateApplicationStatusRequest request) throws SQLException;

    PagedResponse<ApplicationResponse> listAdminApplications(String search,
                                                             String status,
                                                             String degreeLevel,
                                                             Long consultantId,
                                                             boolean unassignedOnly,
                                                             int page,
                                                             int pageSize) throws SQLException;

    Map<String, Object> getAdminApplicationDetail(long applicationId) throws SQLException;

    Map<String, Object> updateAdminApplicationStatus(long adminId,
                                                     long applicationId,
                                                     UpdateApplicationStatusRequest request) throws SQLException;

    Map<String, Object> assignConsultant(long adminId,
                                         long applicationId,
                                         AssignConsultantRequest request) throws SQLException;

    Map<String, Object> removeConsultantAssignment(long adminId,
                                                   long applicationId,
                                                   String note) throws SQLException;
}
