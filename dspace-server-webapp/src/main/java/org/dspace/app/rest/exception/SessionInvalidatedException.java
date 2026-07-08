package org.dspace.app.rest.exception;

public class SessionInvalidatedException extends RuntimeException{

    public SessionInvalidatedException(String message){
        super(message);
    }

}
