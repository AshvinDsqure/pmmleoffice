package org.dspace.app.rest.model;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class EmasResponse {
    private int status;
    private String message;
    private Object data;

    public EmasResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }


    public static ResponseEntity<EmasResponse> buildErrorResponse(String message) {
        EmasResponse response = new EmasResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    public static ResponseEntity<EmasResponse> buildBadRequestResponse(String message) {
        EmasResponse response = new EmasResponse(HttpStatus.BAD_REQUEST.value(), message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
