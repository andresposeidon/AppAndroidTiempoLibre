package com.tiempolibre;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class carga_inicio extends Activity {

	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.carga);
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        
	        
	        //Vamos a declarar un nuevo thread
	        Thread timer = new Thread(){
	            //El nuevo Thread exige el metodo run
	            public void run(){
	                try{
	                    sleep(2000);
	                }catch(InterruptedException e){
	                    //Si no puedo ejecutar el sleep muestro el error
	                    e.printStackTrace();
	                }finally{
	                    Intent actividaPrincipal = new Intent("com.tiempolibre.Actividades");
	                    startActivity(actividaPrincipal);
	                    finish();  
	                   
	                }                
	            }
	        };
	        timer.start();
	    }
	    
	}
