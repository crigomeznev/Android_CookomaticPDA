/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.cookomaticpda.model.exception;

/**
 *
 * @author Usuari
 */
public class CookomaticException extends RuntimeException{

    public CookomaticException(String message) {
        super(message);
    }

    public CookomaticException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
