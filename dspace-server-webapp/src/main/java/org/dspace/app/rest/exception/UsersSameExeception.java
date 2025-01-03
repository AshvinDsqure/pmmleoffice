package org.dspace.app.rest.exception;

public class UsersSameExeception extends RuntimeException{

    public UsersSameExeception(String message){
        super(message);
    }
    public UsersSameExeception(String message, Throwable cause) {
        super(message, cause); // pass the message and cause to the parent class (Exception)
    }

}