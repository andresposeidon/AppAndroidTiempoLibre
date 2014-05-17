package com.tiempolibre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class Actividades extends ListActivity {

	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	ArrayList<HashMap<String, String>> lisactividades;

	private static String url_all_actividades = "http://mucaparadise.com/tiempolibre/android/lista_actividades_completa.php";

	// JSON Node names
	private static final String TAG_actividades = "lista_actividades";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_IMG = "" + R.drawable.logo;
	private static final String TAG_id_act = "id_act";
	private static final String TAG_titulo = "titulo";
	private static final String TAG_detalle = "detalle";
	private static final String TAG_descripcion = "descripcion";
	private static final String TAG_tipo = "tipo";
	private static final String TAG_imagen = "imagen";
	private static final String TAG_precio = "precio";
	private static final String TAG_fecha = "fecha";
	private static final String TAG_hora = "hora";
	private static final String TAG_latitud = "latitud";
	private static final String TAG_longitud = "longitud";
	private static final String TAG_direccion = "direccion";
	private static final String TAG_url = "url";

	private Button btnMapa;
	private Button btnOrigen;
	private Button btnActualiza;

	private SQLiteDatabase db;
	private ActividadesSQLiteHelper usdbh;

	// actividades JSONArray
	JSONArray actividades = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_actividades);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		btnMapa = (Button) findViewById(R.id.actmapa);
		btnOrigen = (Button) findViewById(R.id.actorigen);
		btnActualiza = (Button) findViewById(R.id.actactualiza);

		// Abrimos la base de datos 'DBActividadess' en modo escritura
		usdbh = new ActividadesSQLiteHelper(this, "DBActividades", null, 1);

		db = usdbh.getWritableDatabase();

		// Hashmap for ListView
		lisactividades = new ArrayList<HashMap<String, String>>();

		// mensajes push https://parse.com
		Parse.initialize(this, "Ik0AHto53S8qXJBOfq8ovpAhW7yQIqC5p4AIw6Iw",
				"RYPqRyUKiNTHzGaqdosXH5rupZW9LYdozCJR7g6S");
		PushService.setDefaultPushCallback(this, Actividades.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();

		ParseAnalytics.trackAppOpened(getIntent());

		if (verificaConexion(this)) {
			new LoadAllempleados().execute();
		} else {
			new LoadBD().execute();
		}
		// Get listview
		ListView lv = getListView();

		// on seleting single actividad
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting values from selected ListItem
				// String cedula = ((TextView)
				// view.findViewById(R.id.p_latitud)).getText()
				// .toString();
				// Toast toast = Toast.makeText(EmpleadosActivity.this, texto,
				// Toast.LENGTH_LONG);
				// toast.show();

				Intent i = new Intent(Actividades.this,
						DescripcionAtividad.class);
				i.putExtra("id_act", ((TextView) view.findViewById(R.id.p_ide))
						.getText().toString());
				startActivity(i);
			}
		});

		btnMapa.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Actividades.this, Mapa_act.class);
				startActivity(i);
			}
		});


		btnOrigen.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent actividaPrincipal = new Intent(
						"com.tiempolibre.Desarrolladores");
				startActivity(actividaPrincipal);

			}
		});

		btnActualiza.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Intent i = new Intent(Actividades.this, Actividades.class);
				startActivity(i);
				finish();
			}
		});

	}

	// Response from Edit Empleado Activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// if result code 100
		if (resultCode == 100) {
			// if result code 100 is received
			// means user edited/deleted Empleado
			// reload this screen again
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}

	}

	class LoadAllempleados extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Actividades.this);
			pDialog.setMessage("Cargando Actividades. Vía Web...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_actividades,
					"GET", params);

			// Check your log cat for JSON reponse
			Log.d("All empleados: ", json.toString());

			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// actividades found
					// Getting Array of actividades
					actividades = json.getJSONArray(TAG_actividades);

					// bD actualizar
					usdbh.onUpgrade(db, 1, 1);

					// looping through All actividades
					for (int i = 0; i < actividades.length(); i++) {
						JSONObject c = actividades.getJSONObject(i);

						// Storing each json item in variable
						String s_titulo = c.getString(TAG_titulo);
						String s_detalle = c.getString(TAG_fecha) + " - "
								+ c.getString(TAG_hora) + " "
								+ c.getString(TAG_detalle);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value

						map.put(TAG_detalle, s_detalle);
						map.put(TAG_titulo, s_titulo);
						map.put(TAG_id_act, c.getString(TAG_id_act));

						if (c.getString(TAG_tipo).equals("MUSICA")) {
							map.put(TAG_IMG, "" + R.drawable.musica);
						}
						if (c.getString(TAG_tipo).equals("TEATRO")) {
							map.put(TAG_IMG, "" + R.drawable.teatro);
						}
						if (c.getString(TAG_tipo).equals("DANZA")) {
							map.put(TAG_IMG, "" + R.drawable.danza);
						}
						if (c.getString(TAG_tipo).equals("SEMINARIOS")) {
							map.put(TAG_IMG, "" + R.drawable.seminario);
						}
						if (c.getString(TAG_tipo).equals("LIBROS")) {
							map.put(TAG_IMG, "" + R.drawable.libros);
						}
						if (c.getString(TAG_tipo).equals("CIENCIAS")) {
							map.put(TAG_IMG, "" + R.drawable.ciencia);
						}
						if (c.getString(TAG_tipo).equals("DEPORTES")) {
							map.put(TAG_IMG, "" + R.drawable.deporte);
						}
						if (c.getString(TAG_tipo).equals("MASCOTAS")) {
							map.put(TAG_IMG, "" + R.drawable.mascotas);
						}
						if (c.getString(TAG_tipo).equals("AUDIOVISUAL")) {
							map.put(TAG_IMG, "" + R.drawable.audiovisual);
						}

						// adding HashList to ArrayList
						lisactividades.add(map);

						// Alternativa 2: método insert()
						ContentValues nuevoRegistro = new ContentValues();
						nuevoRegistro.put("id_act",
								Integer.parseInt(c.getString(TAG_id_act)));
						nuevoRegistro.put("titulo", c.getString(TAG_titulo));
						nuevoRegistro.put("detalle", c.getString(TAG_detalle));
						nuevoRegistro.put("descripcion",
								c.getString(TAG_descripcion));
						nuevoRegistro.put("tipo", c.getString(TAG_tipo));
						nuevoRegistro.put("imagen", c.getString(TAG_imagen));
						nuevoRegistro.put("precio",
								Integer.parseInt(c.getString(TAG_precio)));
						nuevoRegistro.put("fecha", c.getString(TAG_fecha));
						nuevoRegistro.put("hora", c.getString(TAG_hora));
						nuevoRegistro.put("latitud",
								Double.parseDouble(c.getString(TAG_latitud)));
						nuevoRegistro.put("longitud",
								Double.parseDouble(c.getString(TAG_longitud)));
						nuevoRegistro.put("direccion",
								c.getString(TAG_direccion));
						nuevoRegistro.put("url", c.getString(TAG_url));

						db.insert("actividadesSQ", null, nuevoRegistro);
					}

				} else {

					// /
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all empleados
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(Actividades.this,
							lisactividades, R.layout.list_item, new String[] {
									TAG_titulo, TAG_detalle, TAG_IMG,
									TAG_id_act }, new int[] { R.id.titulo,
									R.id.detalle, R.id.imageView_imagen,
									R.id.p_ide });
					// updating listview
					setListAdapter(adapter);
				}
			});

		}

	}

	class LoadBD extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Actividades.this);
			pDialog.setMessage("Cargando Actividades. Sin conexión a Internet...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... args) {

			// Alternativa 1: método rawQuery()
			Cursor c = db
					.rawQuery(
							"SELECT id_act, titulo, detalle, tipo, precio, fecha, hora, latitud, longitud, direccion, url FROM actividadesSQ",
							null);

			// Recorremos los resultados para mostrarlos en pantalla
			if (c.moveToFirst()) {
				// Recorremos el cursor hasta que no haya más registros
				do {

					// creating new HashMap
					HashMap<String, String> map = new HashMap<String, String>();

					map.put(TAG_detalle,
							c.getString(5) + " - " + c.getString(6) + " "
									+ c.getString(2));
					map.put(TAG_titulo, c.getString(1));
					map.put(TAG_id_act, c.getString(0));

					if (c.getString(3).equals("MUSICA")) {
						map.put(TAG_IMG, "" + R.drawable.musica);
					}
					if (c.getString(3).equals("TEATRO")) {
						map.put(TAG_IMG, "" + R.drawable.teatro);
					}
					if (c.getString(3).equals("DANZA")) {
						map.put(TAG_IMG, "" + R.drawable.danza);
					}
					if (c.getString(3).equals("SEMINARIOS")) {
						map.put(TAG_IMG, "" + R.drawable.seminario);
					}
					if (c.getString(3).equals("LIBROS")) {
						map.put(TAG_IMG, "" + R.drawable.libros);
					}
					if (c.getString(3).equals("CIENCIAS")) {
						map.put(TAG_IMG, "" + R.drawable.ciencia);
					}
					if (c.getString(3).equals("DEPORTES")) {
						map.put(TAG_IMG, "" + R.drawable.deporte);
					}
					if (c.getString(3).equals("MASCOTAS")) {
						map.put(TAG_IMG, "" + R.drawable.mascotas);
					}
					if (c.getString(3).equals("AUDIOVISUAL")) {
						map.put(TAG_IMG, "" + R.drawable.audiovisual);
					}

					// adding HashList to ArrayList
					lisactividades.add(map);

				} while (c.moveToNext());
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all empleados
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed JSON data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(Actividades.this,
							lisactividades, R.layout.list_item, new String[] {
									TAG_titulo, TAG_detalle, TAG_IMG,
									TAG_id_act }, new int[] { R.id.titulo,
									R.id.detalle, R.id.imageView_imagen,
									R.id.p_ide });
					// updating listview
					setListAdapter(adapter);
				}
			});

		}

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
}
