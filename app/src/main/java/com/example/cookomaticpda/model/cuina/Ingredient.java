/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.cookomaticpda.model.cuina;

import java.io.Serializable;

/**
 *
 * @author Usuari
 */
public class Ingredient implements Serializable{
    private long codi;
    private String nom;

    protected Ingredient() {
    }
    
    public Ingredient(long codi, String nom) {
        setCodi(codi);
        setNom(nom);
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
        this.nom = nom;
    }

    @Override
    public String toString() {
        return nom;
    }
    
    
    
}
