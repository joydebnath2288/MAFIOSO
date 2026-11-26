package com.mafioso.service;

import com.mafioso.dto.StudentProfileRequest;
import com.mafioso.dto.StudentProfileResponse;

import java.sql.SQLException;

/**
 * Service contract for reading and updating student dossier profiles.
 */
public interface StudentProfileService {

    StudentProfileResponse getOwnProfile(long studentId) throws SQLException;

    StudentProfileResponse updateOwnProfile(long studentId, StudentProfileRequest request) throws SQLException;
}
