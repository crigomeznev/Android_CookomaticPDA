/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.cookomaticpda.model.sala;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Usuari
 */
public class Comanda implements Serializable{
    private long codi;
    private Date data;
    private int taula;
    private Cambrer cambrer;
    private List<LiniaComanda> linies;
    
    protected Comanda() {
    }

    public Comanda(long codi, Date data, int taula, Cambrer cambrer) {
        this.codi = codi;
        this.data = data;
        this.taula = taula;
        this.cambrer = cambrer;
    }
    
    public long getCodi() {
        return codi;
    }

    public void setCodi(long codi) {
        this.codi = codi;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public int getTaula() {
        return taula;
    }

    public void setTaula(int taula) {
        this.taula = taula;
    }

    public Cambrer getCambrer() {
        return cambrer;
    }

    public void setCambrer(Cambrer cambrer) {
        this.cambrer = cambrer;
    }
    
    public Iterator<LiniaComanda> iteLinies() {
        return linies.iterator();
    }
    
    public boolean addLinia(LiniaComanda linia) {
        return linies.add(linia);
    }

    public boolean removeLinia(LiniaComanda linia) {
        return linies.remove(linia);
    }

    public BigDecimal getBaseImposable() {
        return null;
    }

    public BigDecimal getIVA() {
        return null;
    }

}
