package org.dspace.app.rest.exception;

public class TokenNotFoundException extends RuntimeException{

    public TokenNotFoundException(String message){
        super(message);
    }
    public TokenNotFoundException(String message, Throwable cause) {
        super(message, cause); // pass the message and cause to the parent class (Exception)
    }

}
