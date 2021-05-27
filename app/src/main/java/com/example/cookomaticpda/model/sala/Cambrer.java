/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.cookomaticpda.model.sala;

import java.io.Serializable;
import com.example.cookomaticpda.model.exception.*;

/**
 *
 * @author Gomez_Nevado
 */
public class Cambrer implements Serializable {
    private long codi;
    private String nom;
    private String cognom1;
    private String cognom2;
    private String user;
    private String password;

    protected Cambrer() {
    }

    public Cambrer(long codi, String nom, String cognom1, String cognom2, String user, String password) {
        setCodi(codi);
        setNom(nom);
        setCognom1(cognom1);
        setCognom2(cognom2);
        setUser(user);
        setPassword(password);
    }
    
    public long getCodi() {
        return codi;
    }

    public void setCodi(long codi) {
        this.codi = codi;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom==null) throw new CookomaticException("Nom obligatori");
        this.nom = nom;
    }

    public String getCognom1() {
        return cognom1;
    }

    public void setCognom1(String cognom1) {
        if (cognom1==null) throw new CookomaticException("Cognom1 obligatori");
        this.cognom1 = cognom1;
    }

    public String getCognom2() {
        return cognom2;
    }

    public void setCognom2(String cognom2) {
        this.cognom2 = cognom2;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        if (user==null) throw new CookomaticException("User obligatori");
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password==null) throw new CookomaticException("Password obligatori");
        this.password = password;
    }
    
    
}
