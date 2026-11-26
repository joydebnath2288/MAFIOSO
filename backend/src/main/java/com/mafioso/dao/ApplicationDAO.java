package com.mafioso.dao;

import com.mafioso.model.Application;
import com.mafioso.model.ApplicationStatus;
import com.mafioso.model.ApplicationStatusHistory;
import com.mafioso.model.DegreeLevel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * JDBC persistence contract for {@code applications} and {@code application_status_history}.
 */
public interface ApplicationDAO {

    String nextReferenceNumber(Connection connection, int year) throws SQLException;

    Application create(Connection connection, Application application) throws SQLException;

    Application findById(Connection connection, long applicationId) throws SQLException;

    List<Application> findByStudentId(Connection connection,
                                      long studentId,
                                      ApplicationStatus statusFilter,
                                      int offset,
                                      int limit) throws SQLException;

    long countByStudentId(Connection connection, long studentId, ApplicationStatus statusFilter) throws SQLException;

    List<Application> findByConsultantId(Connection connection,
                                         long consultantId,
                                         String search,
                                         ApplicationStatus statusFilter,
                                         int offset,
                                         int limit) throws SQLException;

    long countByConsultantId(Connection connection,
                             long consultantId,
                             String search,
                             ApplicationStatus statusFilter) throws SQLException;

    List<Application> findAll(Connection connection,
                              String search,
                              ApplicationStatus statusFilter,
                              DegreeLevel degreeFilter,
                              Long consultantId,
                              boolean unassignedOnly,
                              int offset,
                              int limit) throws SQLException;

    long countAll(Connection connection,
                  String search,
                  ApplicationStatus statusFilter,
                  DegreeLevel degreeFilter,
                  Long consultantId,
                  boolean unassignedOnly) throws SQLException;

    boolean updateStatus(Connection connection, long applicationId, ApplicationStatus newStatus) throws SQLException;

    boolean updateAssignedConsultant(Connection connection, long applicationId, Long consultantId) throws SQLException;

    void addStatusHistory(Connection connection, ApplicationStatusHistory history) throws SQLException;

    List<ApplicationStatusHistory> findStatusHistoryByApplicationId(Connection connection, long applicationId) throws SQLException;

    Map<String, Long> getApplicationMetrics(Connection connection) throws SQLException;

    Map<String, Long> getConsultantApplicationMetrics(Connection connection, long consultantId) throws SQLException;
}
