package com.tiempolibre;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Mapa_act extends FragmentActivity implements
OnMapClickListener {

		private LatLng UBB;

		private GoogleMap mapa;
		
		private SQLiteDatabase db;
		private ActividadesSQLiteHelper usdbh;
		private ArrayList<LatLng> markerPoints;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			
			// Initializing array List
		    markerPoints = new ArrayList<LatLng>();
		    

			super.onCreate(savedInstanceState);

			setContentView(R.layout.activity_mapa);
			
			mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

			mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);


			mapa.setMyLocationEnabled(true);

			mapa.getUiSettings().setZoomControlsEnabled(true);
			
			mapa.getUiSettings().setScrollGesturesEnabled(true);

			mapa.getUiSettings().setCompassEnabled(true);
			
			//Abrimos la base de datos 'DBUsuarios' en modo escritura
			 usdbh = new ActividadesSQLiteHelper(this, "DBActividades", null, 1);
			 
			 db = usdbh.getWritableDatabase();
			 
			//Alternativa 1: método rawQuery()
			Cursor c = db.rawQuery("SELECT id_act, titulo, detalle, tipo, precio, fecha, hora, latitud, longitud, direccion FROM actividadesSQ", null);
			
			//Recorremos los resultados para mostrarlos en pantalla
			if (c.moveToFirst()) {
			     //Recorremos el cursor hasta que no haya más registros
			     do {
			          	          
			    	 mapa.addMarker(new MarkerOptions()
			    	 .position(new LatLng(Double.parseDouble(c.getString(7)), Double.parseDouble(c.getString(8))))
						.title(c.getString(1))

						.snippet(c.getString(9) )
			
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))

						.anchor(0.5f, 0.5f));	
			    	 UBB = new LatLng(Double.parseDouble(c.getString(7)), Double.parseDouble(c.getString(8)));
			          
			     } while(c.moveToNext());
			}
			
			
			
			
			if(getIntent().getExtras()!=null){
				
				Bundle bundle = getIntent().getExtras();
				
				   UBB = new LatLng(bundle.getDouble("latitud"), bundle.getDouble("longitud"));
				    
				    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(UBB, 15));
			}
			else{
			    
			    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(UBB, 15));
			}
			 

			mapa.setOnMapClickListener(this);
			mapa.setOnMarkerClickListener(new OnMarkerClickListener() {
			    public boolean onMarkerClick(Marker marker) {
			    	if(mapa.getMyLocation() != null){
			   		    
			    		mapa.clear();
			    		
			    		//Abrimos la base de datos 'DBUsuarios' en modo escritura
						 usdbh = new ActividadesSQLiteHelper(Mapa_act.this, "DBActividades", null, 1);
						 
						 db = usdbh.getWritableDatabase();
						//Alternativa 1: método rawQuery()
						Cursor c = db.rawQuery("SELECT id_act, titulo, detalle, tipo, precio, fecha, hora, latitud, longitud, direccion FROM actividadesSQ", null);
						
						//Recorremos los resultados para mostrarlos en pantalla
						if (c.moveToFirst()) {
						     //Recorremos el cursor hasta que no haya más registros
						     do {
						          	          
						    	 mapa.addMarker(new MarkerOptions()
						    	 .position(new LatLng(Double.parseDouble(c.getString(7)), Double.parseDouble(c.getString(8))))
									.title(c.getString(1))

									.snippet(c.getString(9) )
						
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher))

									.anchor(0.5f, 0.5f));	
						          
						     } while(c.moveToNext());
						}
			    		
		                LatLng origin =new LatLng(mapa.getMyLocation().getLatitude(),  mapa.getMyLocation().getLongitude());
		            
		                LatLng dest = marker.getPosition();

		                String url = getDirectionsUrl(origin, dest);

		                DownloadTask downloadTask = new DownloadTask();

		                downloadTask.execute(url);
		            }
			 
			        return false;
			    }
			});
				
	
			 

		}
		
		public void onMarkerClick(Marker marker) {
			 LatLng origin =new LatLng(mapa.getMyLocation().getLatitude(),  mapa.getMyLocation().getLongitude());
	            
             LatLng dest = marker.getPosition();

             String url = getDirectionsUrl(origin, dest);

             DownloadTask downloadTask = new DownloadTask();

             downloadTask.execute(url);         
		}

		public void moveCamera(View view) {

			mapa.animateCamera(CameraUpdateFactory.newLatLng(UBB));

		}

		public void animateCamera(View view) {

			if (mapa.getMyLocation() != null)

				mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
						mapa.getMyLocation().getLatitude(), mapa.getMyLocation()
								.getLongitude()), 15));
			

		}

		public void addMarker(View view) {

			mapa.addMarker(new MarkerOptions().position(

			new LatLng(mapa.getCameraPosition().target.latitude,

			mapa.getCameraPosition().target.longitude)));

		}

		@Override
		public void onMapClick(LatLng puntoPulsado) {

			
		}
	
	
	private String getDirectionsUrl(LatLng origin,LatLng dest){

	    // Origin of route
	    String str_origin = "origin="+origin.latitude+","+origin.longitude;

	    // Destination of route
	    String str_dest = "destination="+dest.latitude+","+dest.longitude;

	    // Sensor enabled
	    String sensor = "sensor=false";

	    // Building the parameters to the web service
	    String parameters = str_origin+"&"+str_dest+"&"+sensor;

	    // Output format
	    String output = "json";

	    String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

	    return url;
	}

	private String downloadUrl(String strUrl) throws IOException{
	    String data = "";
	    InputStream iStream = null;
	    HttpURLConnection urlConnection = null;
	    try{
	        URL url = new URL(strUrl);

	        urlConnection = (HttpURLConnection) url.openConnection();

	        // Connecting to url
	        urlConnection.connect();

	        // Reading data from url
	        iStream = urlConnection.getInputStream();

	        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

	        StringBuffer sb  = new StringBuffer();

	        String line = "";
	        while( ( line = br.readLine())  != null){
	            sb.append(line);
	        }

	        data = sb.toString();

	        br.close();

	    }catch(Exception e){
	        Log.d("Exception while downloading url", e.toString());
	    }finally{
	        iStream.close();
	        urlConnection.disconnect();
	    }
	    return data;
	}

	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String>{

	    // Downloading data in non-ui thread
	    @Override
	    protected String doInBackground(String... url) {

	        // For storing data from web service
	        String data = "";

	        try{
	            // Fetching the data from web service
	            data = downloadUrl(url[0]);
	        }catch(Exception e){
	            Log.d("Background Task",e.toString());
	        }
	        return data;
	    }

	    // Executes in UI thread, after the execution of
	    // doInBackground()
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);

	        ParserTask parserTask = new ParserTask();

	        // Invokes the thread for parsing the JSON data
	        parserTask.execute(result);
	    }
	}


	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>>{

	    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

	        JSONObject jObject;
	        List<List<HashMap<String, String>>> routes = null;

	        try{
	            jObject = new JSONObject(jsonData[0]);
	            DirectionsJSONParser parser = new DirectionsJSONParser();

	            // Starts parsing data
	            routes = parser.parse(jObject);
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        return routes;
	    }

	    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
	        ArrayList<LatLng> points = null;
	        PolylineOptions lineOptions = null;
	        MarkerOptions markerOptions = new MarkerOptions();

	        // Traversing through all the routes
	        for(int i=0;i<result.size();i++){
	            points = new ArrayList<LatLng>();
	            lineOptions = new PolylineOptions();

	            // Fetching i-th route
	            List<HashMap<String, String>> path = result.get(i);

	            // Fetching all the points in i-th route
	            for(int j=0;j<path.size();j++){
	                HashMap<String,String> point = path.get(j);

	                double lat = Double.parseDouble(point.get("lat"));
	                double lng = Double.parseDouble(point.get("lng"));
	                LatLng position = new LatLng(lat, lng);

	                points.add(position);
	            }

	            // Adding all the points in the route to LineOptions
	            lineOptions.addAll(points);
	            lineOptions.width(2);
	            lineOptions.color(Color.RED);
	        }

	        // Drawing polyline in the Google Map for the i-th route
	        mapa.addPolyline(lineOptions);
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.main, menu);
	    return true;
	}	

}
