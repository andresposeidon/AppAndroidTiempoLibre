package com.tiempolibre;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DescripcionAtividad extends Activity {
	
	private Button btnweb;
	private Button btnmapa;
	private Button btncompartir;
	private TextView titulo;
	private TextView detalle;
	private TextView direccion;
	private ImageView imgImagen; 
	private SQLiteDatabase db;
	private ActividadesSQLiteHelper usdbh;
	private Double longitud;
	private Double latitud;
	private String url;
	private String web_url;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_descripcion);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		btnweb = (Button)findViewById(R.id.desweb);
		btnmapa = (Button)findViewById(R.id.desmapa);
		btncompartir = (Button)findViewById(R.id.descompartir);
		titulo = (TextView)findViewById(R.id.destitulo);
		detalle = (TextView)findViewById(R.id.desdetalle);
		direccion = (TextView)findViewById(R.id.desdireccion);
		imgImagen = (ImageView) findViewById(R.id.desimagen); 
		
		Bundle bundle = getIntent().getExtras();

		//Abrimos la base de datos 'DBUsuarios' en modo escritura
		 usdbh = new ActividadesSQLiteHelper(this, "DBActividades", null, 1);

		 db = usdbh.getWritableDatabase();
       
		Cursor c = db.rawQuery("SELECT id_act, titulo, descripcion, tipo, precio, fecha, hora, latitud, longitud, direccion, imagen, url FROM actividadesSQ WHERE id_act="+bundle.getString("id_act"), null);
		
		if (c.moveToFirst()) {
		     do {
		           	if(c.getString(4).equals("0")){
		           		titulo.setText(""+c.getString(1)+"\n Entrada Liberada");
		           	}else{
		           		titulo.setText(""+c.getString(1)+"\n Entrada $"+c.getString(4));
		           	}
	           		detalle.setText(""+c.getString(5)+" a las "+c.getString(6)+"\n"+c.getString(2));
	           		direccion.setText(c.getString(9));
					latitud= Double.parseDouble(c.getString(7));
		    	    longitud= Double.parseDouble(c.getString(8));
		    	    url= new String(c.getString(10));
		    	    web_url= new String(c.getString(11));
		          
		     } while(c.moveToNext());
		}
		
		if(verificaConexion(DescripcionAtividad.this)){
			CargaImagene nuevaTarea = new CargaImagene();
	        nuevaTarea.execute(url);
		}
		else{
			imgImagen.setImageResource(R.drawable.imgdefault);
		}
		
		btnweb.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {	
				
				if(verificaConexion(DescripcionAtividad.this)){
					Intent i=new Intent(DescripcionAtividad.this, navegador.class);
					i.putExtra("url", web_url);
					startActivity(i);
				}
				else{
					Toast toast = Toast.makeText(DescripcionAtividad.this, "Deshabilitado. Sin conexión a Internet", Toast.LENGTH_LONG);
	                toast.show();
				}
			}
		});
		
		btnmapa.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {	
				Intent i=new Intent(DescripcionAtividad.this, Mapa_act.class);
				 i.putExtra("latitud", latitud);
				i.putExtra("longitud", longitud);
				 startActivity(i);
			}
		});
		
		btncompartir.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {	
				if(verificaConexion(DescripcionAtividad.this)){
					Intent i=new Intent(DescripcionAtividad.this, compartir.class);
					i.putExtra("url", web_url);
					startActivity(i);
				}
				else{
					Toast toast = Toast.makeText(DescripcionAtividad.this, "Deshabilitado. Sin conexión a Internet", Toast.LENGTH_LONG);
	                toast.show();
				}
			}
		});
		   
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public static boolean verificaConexion(Context ctx) {
	    boolean bConectado = false;
	    ConnectivityManager connec = (ConnectivityManager) ctx
	            .getSystemService(Context.CONNECTIVITY_SERVICE);
	    // No sólo wifi, también GPRS
	    NetworkInfo[] redes = connec.getAllNetworkInfo();
	    // este bucle debería no ser tan ñapa
	    for (int i = 0; i < 2; i++) {
	        // ¿Tenemos conexión? ponemos a true
	        if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
	            bConectado = true;
	        }
	    }
	    return bConectado;
	}
	
	public class CargaImagene extends AsyncTask<String, Void, Bitmap>{
   	 
        ProgressDialog pDialog;
     
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
             
            pDialog = new ProgressDialog(DescripcionAtividad.this);
            pDialog.setMessage("Cargando Imagen");
            pDialog.setCancelable(true);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.show();
             
        }
     
        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO Auto-generated method stub
            Log.i("doInBackground" , "Entra en doInBackground");
            String url = params[0];
            Bitmap imagen = descargarImagen(url);
            return imagen;
        }
         
        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
             
            imgImagen.setImageBitmap(result);
            pDialog.dismiss();
        }
    }
        
        private Bitmap descargarImagen (String imageHttpAddress){
            URL imageUrl = null;
            Bitmap imagen = null;
            try{
                imageUrl = new URL(imageHttpAddress);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.connect();
                imagen = BitmapFactory.decodeStream(conn.getInputStream());
            }catch(IOException ex){
                ex.printStackTrace();
            }
             
            return imagen;
        }

}
