package com.project.website.exeption;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends RuntimeException{
    public ForbiddenException(HttpStatus forbidden, String massage){
        super (massage);
    }
}
