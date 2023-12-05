package com.luiscortes.reproductor;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.Arrays;

public class Cancion {
    private final byte[][] idFoto;
    private final String nombre;

    public Cancion(String nombre, byte[][] idFoto) {
        this.idFoto = idFoto;
        this.nombre = nombre;
    }

    public Bitmap getBitmap(int indice){
        Bitmap bitmap;
        byte[] fotos = new byte[idFoto[indice].length];
        for (int i = 0; i < idFoto[indice].length; i++){
            fotos[i] = idFoto[indice][i];
        }
        bitmap = BitmapFactory.decodeByteArray(fotos, 0, fotos.length);
        return bitmap;
    }
    public byte[][] getIdFoto() {
        return idFoto;
    }

    public String getNombre() {
        return nombre;
    }


    @Override
    public String toString() {
        return "Cancion{" +
                "idFoto=" + Arrays.toString(idFoto) +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}
