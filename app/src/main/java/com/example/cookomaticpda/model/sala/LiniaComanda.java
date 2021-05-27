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
public class LiniaComanda implements Serializable {

    private int num;
    private int quantitat;
    private EstatLinia estat;

    protected LiniaComanda() {
    }

    public LiniaComanda(int num, int quantitat, EstatLinia estat) {
        setNum(num);
        setQuantitat(quantitat);
        setEstat(estat);
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getQuantitat() {
        return quantitat;
    }

    public void setQuantitat(int quantitat) {
        this.quantitat = quantitat;
    }

    public EstatLinia getEstat() {
        return estat;
    }

    public void setEstat(EstatLinia estat) {
        this.estat = estat;
    }

}
