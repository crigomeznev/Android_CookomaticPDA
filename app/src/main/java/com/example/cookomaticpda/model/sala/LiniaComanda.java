/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.cookomaticpda.model.sala;

import com.example.cookomaticpda.model.cuina.Plat;

import java.io.Serializable;

/**
 *
 * @author Gomez_Nevado
 */
public class LiniaComanda implements Serializable {

    private int num;
    private int quantitat;
    private EstatLinia estat;
    private Plat item;



    protected LiniaComanda() {
    }

    public LiniaComanda(int num, int quantitat, EstatLinia estat, Plat item) {
        setNum(num);
        setQuantitat(quantitat);
        setEstat(estat);
        setItem(item);
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

    public Plat getItem() {
        return item;
    }

    public void setItem(Plat item) {
        this.item = item;
    }

    // TODO: getImport()


    @Override
    public String toString() {
        return "LiniaComanda{" +
                "num=" + num +
                ", quantitat=" + quantitat +
                ", estat=" + estat +
                ", item=" + item.getNom() +
                '}';
    }
}
