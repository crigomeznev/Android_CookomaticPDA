/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.cookomaticpda.model.sala;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
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
//    private int taula;
    private Taula taula;
    private Cambrer cambrer;
    private List<LiniaComanda> linies;
    private boolean finalitzada;
    
    protected Comanda() {
    }

    public Comanda(long codi, Date data, Taula taula, Cambrer cambrer) {
        setCodi(codi);
        setData(data);
        setTaula(taula);
        setCambrer(cambrer);
        this.finalitzada = false;
        this.linies = new ArrayList<>();
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

    public Taula getTaula() {
        return taula;
    }

    public void setTaula(Taula taula) {
        this.taula = taula;

        // si la comanda no està finalitzada, la comanda activa de la taula serà aquesta
        if (!finalitzada){
            if (this.taula.getComandaActiva() == null) {
                this.taula.setComandaActiva(this);
            }
            else {
                // TODO: només deixarem crear comanda assignada a una taula quan la taula estigui lliure
                // és a dir, no tingui cap comanda activa
            }
        }

    }

    public Cambrer getCambrer() {
        return cambrer;
    }

    public void setCambrer(Cambrer cambrer) {
        this.cambrer = cambrer;
    }

    public boolean isFinalitzada() {
        return finalitzada;
    }

    public void setFinalitzada(boolean finalitzada) {
        this.finalitzada = finalitzada;

        // comanda finalitzada, la taula queda lliure
        if (this.finalitzada)
            this.taula.setComandaActiva(null);
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
