package org.dspace.app.rest.exception;

public class DataNotFoundExpetion extends RuntimeException{
    public DataNotFoundExpetion(String message){
        super(message);
    }
}