/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.cookomaticpda.model.cuina;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.example.cookomaticpda.model.exception.*;
/**
 *
 * @author Usuari
 */
public class Plat implements Serializable {
    private long codi;
    private String nom;
    private String descripcioMD;
    private BigDecimal preu;
//    private BufferedImage foto;
    private boolean disponible;

    
    private Categoria categoria;
    
    
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(name = "linia_escandall", joinColumns = @JoinColumn(name = "plat"))
    private List<LiniaEscandall> escandall;

    // variables auxiliars
    private Blob fotoBlob;

    protected Plat() {
    }

//    public Plat(long codi, String nom, String descripcioMD, BigDecimal preu, BufferedImage foto, boolean disponible, Categoria categoria, List<LiniaEscandall> escandall) {
//        setCodi(codi);
//        setNom(nom);
//        setDescripcioMD(descripcioMD);
//        setPreu(preu);
//        setFoto(foto);
//        setDisponible(disponible);
//        setCategoria(categoria);
//        this.escandall = escandall;
//    }
    
    public Plat(long codi, String nom, String descripcioMD, BigDecimal preu, Blob fotoBlob, boolean disponible, Categoria categoria, List<LiniaEscandall> escandall) {
        setCodi(codi);
        setNom(nom);
        setDescripcioMD(descripcioMD);
        setPreu(preu);
        setFotoBlob(fotoBlob);
        setDisponible(disponible);
        setCategoria(categoria);
        this.escandall = escandall;
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

    public String getDescripcioMD() {
        return descripcioMD;
    }

    public void setDescripcioMD(String descripcioMD) {
        this.descripcioMD = descripcioMD;
    }

    public BigDecimal getPreu() {
        return preu;
    }

    public void setPreu(BigDecimal preu) {
        this.preu = preu;
    }

//    public BufferedImage getFoto() {
//        // només carregarem en memòria la foto quan ens la demanin
//        if (foto==null) {
//            byte[] fotoBytes;
//            try {
//                // Read BLOB into byte-Array
//                fotoBytes = fotoBlob.getBytes(0, (int) fotoBlob.length());
//                // convert byte-Array into Buffered Image (Subclass of Image)
//                this.foto = ImageIO.read(new ByteArrayInputStream(fotoBytes));
//            } catch (SQLException | IOException ex) {
//                throw new CookomaticException("Error en carregar foto de la BD", ex);
//            }
//        /*
//        // Fetch BLOB from DB
//        Blob blb = stmt.executeQuery(...).getBlob("blobColumn");
//        // Read BLOB into byte-Array
//        byte[] imagebytes = blb.getBytes(0, blb.length());
//        // convert byte-Array into Buffered Image (Subclass of Image)
//        BufferedImage theImage=ImageIO.read(new ByteArrayInputStream(imagebytes));
//         */
//        }
//        return foto;
//    }
//
//    public void setFoto(BufferedImage foto) {
//        this.foto = foto;
//    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
    

    public Iterator<LiniaEscandall> iteEscandall(){
        return escandall.iterator();
    }
    
//    public void setEscandall (List<LiniaEscandall> escandall){
//        this.escandall = escandall;
//    }

    
/*    
    public boolean addLiniaEscandall(LiniaEscandall liniaEscandall){
        return escandall.add(liniaEscandall);
    }

    public boolean removeLiniaEscandall(LiniaEscandall liniaEscandall){
        return escandall.remove(liniaEscandall);
    }
*/
    public boolean addLiniaEscandall(LiniaEscandall liniaEscandall) {
        if (!escandall.contains(liniaEscandall)) {
            escandall.add(liniaEscandall);
            return true;
        } else {
            return false;
        }
    }

//    public Iterable<LiniaEscandall> getEscandall() {
//        return escandall;
//    }
    
    public boolean removeLiniaEscandall(LiniaEscandall liniaEscandall) {
        return escandall.remove(liniaEscandall);
    }

    
    public Blob getFotoBlob() {
        return fotoBlob;
    }

    public void setFotoBlob(Blob fotoBlob) {
        this.fotoBlob = fotoBlob;
    }

    @Override
    public String toString() {
        return nom;
    }

    public List<LiniaEscandall> getEscandall() {
        return escandall;
    }

    public void setEscandall(List<LiniaEscandall> escandall) {
        this.escandall = escandall;
    }
    
    
    
    
    public int getPrimerLiniaEscandallNum(){
        int maxNum = escandall.size();
//        int num;
        int numero = -1;
        
        for(int i = 1; i < maxNum; i++){
            for(LiniaEscandall le : escandall){
                if (le.getNum()==i)
                    break;
                
                // si hem arribat al final de les linies i no hi ha cap linia amb aquest numero, retornem aquest numero
                numero = i;
            }
        }
        return numero;
    }


    // retorna el número de línia d'escandall més baix
    public int getMinimNumLiniaEscandall(){
        int i = 0;
        // inicialitzem al primer valor de la llista
        int minim = escandall.get(0).getNum();
        
        for(LiniaEscandall le : escandall) {
            if (le.getNum() < minim){
                minim = le.getNum();
            }
        }
        
        return minim;
    }
    
    
    // retorna true si ja existeix una linia d'escandall amb aquest numero
    public boolean numeroLiniaEscandallExistent(int num){
        boolean trobat = false;
        for(LiniaEscandall le : escandall) {
            if (le.getNum() == num){
                trobat = true;
                break;
            }
        }
        return trobat;
    }
    
    
    
    // retorna el número de línia d'escandall més baix disponible (que no és utilitzat per una altra linia escandall)
    public int getMinimNumDisponibleLiniaEscandall(){
//        int num = getMinimNumLiniaEscandall();
        int num = 1;
        boolean trobat = false;
        
        while (num < escandall.size()+1 && !trobat){
            if (numeroLiniaEscandallExistent(num))
                num++;
            else
                trobat = true;
        }
        
        return num;
    }



    
}
