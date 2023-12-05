package com.luiscortes.reproductor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements CancionAdapter.IOnClickListener{

    private ArrayList<Cancion> canciones;
    private CancionAdapter adapter;
    private MiResultReceiver miResultReceiver;
    private RecyclerView recyclerView;
    private int posicionCancion;
    private TextView tvDatosCancion;

    private ImageView ivFotoCancion;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        canciones = new ArrayList<>();

        miResultReceiver = new MiResultReceiver(null, adapter,recyclerView);

        Intent msgIntent = new Intent(MainActivity.this, MusicPlayerService.class);
        msgIntent.putExtra("receiver", miResultReceiver);
        startService(msgIntent);

        ImageButton ibReproducir = findViewById(R.id.ibReproducir);
        ImageButton ibSiguiente = findViewById(R.id.ibSiguiente);
        ImageButton ibAtras = findViewById(R.id.ibAtras);
        tvDatosCancion = findViewById(R.id.tvDatosCancion);
        ivFotoCancion = findViewById(R.id.ivFotoCancion);

        ivFotoCancion.setImageResource(R.drawable.none);
        tvDatosCancion.setText(" ");



        ibReproducir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopOrPlay();
                    }
                });

            }
        });

        ibSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        siguienteCancion();
                        actualizarDatos();
                    }
                });
            }
        });

        ibAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancionAnterior();
                        actualizarDatos();
                    }
                });
            }
        });
    }


    public void cancionAnterior(){
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ANTERIOR_CANCION);
        intent.putExtra("cancionesSize", String.valueOf(canciones.size()));
        if (posicionCancion > 0 ){
            posicionCancion--;
        } else {
            posicionCancion = canciones.size() - 1;
        }
        intent.putExtra("nombreCancion", canciones.get(posicionCancion).getNombre().toLowerCase());
        startService(intent);
    }

    public void siguienteCancion(){
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.SIGUIENTE_CANCION);
        intent.putExtra("cancionesSize", String.valueOf(canciones.size()));
        if (posicionCancion < canciones.size() -1 ){
            posicionCancion += 1;
        } else {
            posicionCancion = 0;
        }
        intent.putExtra("nombreCancion", canciones.get(posicionCancion).getNombre().toLowerCase());
        startService(intent);
    }
    public void stopOrPlay(){
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.STOP_OR_PLAY);
        startService(intent);
    }

    public void reproducirCancion(Cancion cancion){
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);
        intent.setAction(MusicPlayerService.ACTION_PLAY);
        intent.putExtra("nombreCancion", cancion.getNombre().toLowerCase());
        startService(intent);
    }

    private void actualizarDatos(){
        tvDatosCancion.setText(canciones.get(posicionCancion).getNombre());
        ivFotoCancion.setImageBitmap(canciones.get(posicionCancion).getBitmap(posicionCancion));
    }


    @Override
    public void onItemClick(int position) {
        posicionCancion = position;
        reproducirCancion(canciones.get(position));
        actualizarDatos();
    }


    public class MiResultReceiver extends ResultReceiver {
        private CancionAdapter adapter;
        private final RecyclerView recyclerView;
        public MiResultReceiver(Handler handler, CancionAdapter adapter, RecyclerView recyclerView) {
            super(handler);
            this.adapter = adapter;
            this.recyclerView = recyclerView;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case MusicPlayerService.CODE_CANCION:
                    ArrayList<String> nombres = resultData.getStringArrayList("nombre");
                    byte[][] fotos = (byte[][]) resultData.getSerializable("foto");
                    for (int i = 0; i < nombres.size(); i++) {
                        canciones.add(new Cancion(nombres.get(i), fotos));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new CancionAdapter(canciones, MainActivity.this);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        }
                    });
                    break;
            }
        }
    }
}