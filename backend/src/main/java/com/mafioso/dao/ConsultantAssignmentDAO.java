package com.mafioso.dao;

import com.mafioso.dto.AssignedStudentSummaryResponse;
import com.mafioso.model.ConsultantAssignment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * JDBC persistence contract for {@code consultant_assignments} and caseload queries.
 */
public interface ConsultantAssignmentDAO {

    ConsultantAssignment findActiveByApplicationId(Connection connection, long applicationId) throws SQLException;

    List<ConsultantAssignment> findHistoryByApplicationId(Connection connection, long applicationId) throws SQLException;

    List<ConsultantAssignment> findActiveByConsultantId(Connection connection, long consultantId) throws SQLException;

    void deactivateActiveForApplication(Connection connection, long applicationId, long unassignedByUserId) throws SQLException;

    void insert(Connection connection, ConsultantAssignment assignment) throws SQLException;

    boolean isConsultantAssignedToStudent(Connection connection, long consultantId, long studentId) throws SQLException;

    List<AssignedStudentSummaryResponse> findAssignedStudentsByConsultant(Connection connection,
                                                                        long consultantId,
                                                                        String search,
                                                                        int offset,
                                                                        int limit) throws SQLException;

    long countAssignedStudentsByConsultant(Connection connection, long consultantId, String search) throws SQLException;

    Map<Long, Long> countActiveStudentsByConsultantIds(Connection connection, List<Long> consultantIds) throws SQLException;
}
