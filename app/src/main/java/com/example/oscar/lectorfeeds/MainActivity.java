package com.example.oscar.lectorfeeds;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Iniciar ini =null;
    TextView campo;
    String URLConnect = "http://ep00.epimg.net/rss/elpais/portada_america.xml";
    int ban,k=0;
   FirebaseDatabase database = FirebaseDatabase.getInstance();
   DatabaseReference myRef = database.getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        campo = (TextView) findViewById(R.id.Edidatos);
    }

    public void Iniciar_servicios(View b){
        ini=new Iniciar();
        ini.execute();
        ban =1;
    }
    public void detener(View p){
        if(ban !=0) {
            ini.cancel(true);
            Toast.makeText(getApplicationContext(),"El proceso se ha detenido",Toast.LENGTH_SHORT).show();
            ban=0;
        }else{
            Toast.makeText(getApplicationContext(),"No hay ningun proceso para detener",Toast.LENGTH_SHORT).show();
        }
    }
    public void lim(View l){
        if (ban==0) {
            campo.setText("");
        }else {
            Toast.makeText(getApplicationContext(),"Hay un proceso ejecutandose no se puede limpiar",Toast.LENGTH_SHORT).show();

        }
    }

    public class Iniciar extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            int i,j=0;
            try {
                URL urlConnexion = new URL(URLConnect);
                HttpURLConnection conexion = (HttpURLConnection) urlConnexion.openConnection(); // abrir conexion
                conexion.setRequestProperty("User-Agent","Mozilla/5.0"+
                "(Linuz;Android 1.5; es-Es) Ejemplo Uceva Http");


                String salida="";
                int conectado =conexion.getResponseCode();


                if(conectado==HttpURLConnection.HTTP_OK){// nos conectamos

                    BufferedReader xml = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                    String linea =xml.readLine(); // leemos la primera line del xml que contiene datos de la fuente

                    while (linea != null || linea != ""){
                        if (!isCancelled()) {
                            if (linea.indexOf("<title><![CDATA[") >= 0) { // si es >=0 es por que en la poscicion 0 o sepuerior inicia la linea
                                i = linea.indexOf("<title><![CDATA[") + 16;// inicio de cadea capturar , <title><![CDATA[" tiene 15 caracteres entonces comenzamos en la 16
                                j = linea.indexOf("</title>") - 3; //Busca la cadena </title> y le resta 3 posiciones para no mostrar ]]>

                                salida = linea.substring(i, j);
                                salida += "\n--------------------\n";

                                k++;
                                EscribirFirebase(k,salida);
                                publishProgress(salida); //Imprimo lo que haya en salida
                                Thread.sleep(2000);

                            }
                            linea = xml.readLine(); // la siguiente linea
                        }else{
                            break;
                        }

                    }
                    xml.close();
                }else {
                    salida="Sin conexion";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
          //  publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            campo.append(""+values[0]);

        }


    }

    public void EscribirFirebase(Integer id,String vale){
        myRef.child("feeds").child(""+id).setValue(vale);

    }
}
