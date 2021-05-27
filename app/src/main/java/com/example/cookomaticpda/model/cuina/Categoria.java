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
public class Categoria implements Serializable{
    private long codi;
    private String nom;
//    private Color color;
    private int colorInt;

    protected Categoria() {
    }

//    public Categoria(long codi, String nom, Color color, int colorInt) {
//        setCodi(codi);
//        setNom(nom);
//        setColor(color);
//        setColorInt(colorInt);
//    }

    public Categoria(long codi, String nom, int colorInt) {
        setCodi(codi);
        setNom(nom);
        setColorInt(colorInt);
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

//    public Color getColor() {
//        return color;
//    }
//
//    public void setColor(Color color) {
//        this.color = color;
//    }

    public int getColorInt() {
        return colorInt;
    }

    public void setColorInt(int colorInt) {
        this.colorInt = colorInt;
        
//        setColor(new Color(colorInt));
    }

    @Override
    public String toString() {
        return nom;
    }
    
    
}
