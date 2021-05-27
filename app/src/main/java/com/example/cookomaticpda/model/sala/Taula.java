/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.cookomaticpda.model.sala;

import java.io.Serializable;

/**
 *
 * @author Gomez_Nevado
 */
public class Taula implements Serializable {
    private int numero;
    private Comanda comandaActiva;

    protected Taula() {
    }
    
    public Taula(int numero) {
        setNumero(numero);
    }
    
    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Comanda getComandaActiva() {
        return comandaActiva;
    }

    public void setComandaActiva(Comanda comandaActiva) {
        this.comandaActiva = comandaActiva;
    }
    
    
}
