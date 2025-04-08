package org.dspace.app.rest.exception;

public class HtmlToPdfConvertException extends RuntimeException{

    public HtmlToPdfConvertException(String message){
        super(message);
    }
    public HtmlToPdfConvertException(String message, Throwable cause) {
        super(message, cause); // pass the message and cause to the parent class (Exception)
    }
}
