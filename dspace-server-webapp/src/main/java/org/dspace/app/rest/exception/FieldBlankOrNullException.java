package org.dspace.app.rest.exception;

public class FieldBlankOrNullException extends Exception{

    public FieldBlankOrNullException() {
        super();
    }
    public FieldBlankOrNullException(String message) {
        super(message);
    }
    public FieldBlankOrNullException(String message, Throwable cause) {
        super(message, cause);
    }
}
