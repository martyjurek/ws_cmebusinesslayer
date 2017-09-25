package com.misys.cmeobject.search;

/**
 * An exception containing additional information pertaining to query generation failures.
 */
public class QueryException extends Exception {
    private String code;
    private String status;

    public QueryException(String code, String status, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = code;
    }
    public QueryException(String code, String status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
