package org.dspace.app.rest.exception;

public class JBPMServerExpetion extends RuntimeException{

    public JBPMServerExpetion(String message){
        super(message);
    }
    public JBPMServerExpetion(String message, Throwable cause) {
        super(message, cause); // pass the message and cause to the parent class (Exception)
    }

}
