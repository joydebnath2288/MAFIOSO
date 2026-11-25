package com.mafioso.dto;

/**
 * Response payload for {@code GET /api/v1/auth/csrf}.
 */
public class CsrfResponse {

    private String csrfToken;
    private String headerName;

    public CsrfResponse() {
    }

    public CsrfResponse(String csrfToken, String headerName) {
        this.csrfToken = csrfToken;
        this.headerName = headerName;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
}
