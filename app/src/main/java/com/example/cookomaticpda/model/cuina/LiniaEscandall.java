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
public class LiniaEscandall implements Serializable{
    private int num;
    private int quantitat;
    private Ingredient ingredient;
    private Unitat unitat;

    protected LiniaEscandall() {
    }

    public LiniaEscandall(int num, int quantitat, Ingredient ingredient, Unitat unitat) {
        setNum(num);
        setQuantitat(quantitat);
        setIngredient(ingredient);
        setUnitat(unitat);
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

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Unitat getUnitat() {
        return unitat;
    }

    public void setUnitat(Unitat unitat) {
        this.unitat = unitat;
    }

    @Override
    public String toString() {
        return "LiniaEscandall{" + "num=" + num + ", quantitat=" + quantitat + ", ingredient=" + ingredient.getNom() + ", unitat=" + unitat.getNom() + '}';
    }


}
