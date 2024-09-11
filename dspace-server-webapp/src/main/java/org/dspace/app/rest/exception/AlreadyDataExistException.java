package org.dspace.app.rest.exception;

public class AlreadyDataExistException extends RuntimeException{
    public AlreadyDataExistException(String message){
        super(message);
    }
}