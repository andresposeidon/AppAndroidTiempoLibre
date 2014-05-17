package com.tiempolibre;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ActividadesSQLiteHelper extends SQLiteOpenHelper {
 
    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE actividadesSQ (id_act integer primary key, titulo TEXT, detalle TEXT, descripcion TEX, tipo TEXT , imagen TEXT, precio integer, fecha TEXT, hora TEXT, latitud Double, longitud Double, direccion TEXT, url TEXT)";
 
    public ActividadesSQLiteHelper(Context contexto, String nombre,
                               CursorFactory factory, int version) {
        super(contexto, nombre, factory, version);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creación de la tabla
        db.execSQL(sqlCreate);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, 
                          int versionNueva) {
 
        //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS actividadesSQ");
 
        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }
}
