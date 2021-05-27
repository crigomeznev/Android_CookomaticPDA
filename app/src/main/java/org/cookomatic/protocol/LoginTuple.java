//package com.example.cookomaticpda.borrar;
package org.cookomatic.protocol;
import java.io.Serializable;

public class LoginTuple implements Serializable {
    private String user;
    private String password;
    private Long sessionId;


    public LoginTuple(String user, String password, Long sessionId) {
        setUser(user);
        setPassword(password);
        setSessionId(sessionId);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }


}
