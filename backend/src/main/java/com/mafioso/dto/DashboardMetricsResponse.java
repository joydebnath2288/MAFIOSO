package com.mafioso.dto;

/**
 * Database-backed aggregate account metrics for the admin dashboard overview.
 */
public class DashboardMetricsResponse {

    private long totalStudents;
    private long activeStudents;
    private long totalConsultants;
    private long activeConsultants;
    private long totalAdministrators;

    public DashboardMetricsResponse() {
    }

    public DashboardMetricsResponse(long totalStudents,
                                    long activeStudents,
                                    long totalConsultants,
                                    long activeConsultants,
                                    long totalAdministrators) {
        this.totalStudents = totalStudents;
        this.activeStudents = activeStudents;
        this.totalConsultants = totalConsultants;
        this.activeConsultants = activeConsultants;
        this.totalAdministrators = totalAdministrators;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getActiveStudents() {
        return activeStudents;
    }

    public void setActiveStudents(long activeStudents) {
        this.activeStudents = activeStudents;
    }

    public long getTotalConsultants() {
        return totalConsultants;
    }

    public void setTotalConsultants(long totalConsultants) {
        this.totalConsultants = totalConsultants;
    }

    public long getActiveConsultants() {
        return activeConsultants;
    }

    public void setActiveConsultants(long activeConsultants) {
        this.activeConsultants = activeConsultants;
    }

    public long getTotalAdministrators() {
        return totalAdministrators;
    }

    public void setTotalAdministrators(long totalAdministrators) {
        this.totalAdministrators = totalAdministrators;
    }
}
