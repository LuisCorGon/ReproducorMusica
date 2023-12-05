package com.luiscortes.reproductor;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicPlayerService extends Service {

    private MediaMetadataRetriever cargarData;
    private ResultReceiver receiver;
    private MediaPlayer mediaPlayer;
    public static final String STOP_OR_PLAY = "com.luiscortes.reproductor.stop_or_play";
    public static final String ACTION_PLAY = "com.luiscortes.reproductor.action_play";
    public static final String SIGUIENTE_CANCION = "com.luiscortes.reproductor.siguiente_cancion";
    public static final String ANTERIOR_CANCION = "com.luiscortes.reproductor.anterior_cancion";
    public static final int CODE_CANCION = 0;
    public String cancionActual;
    private int posicionActual = 0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void anteriorCancion(String sizeCanciones, String nombreCancion){
        if (mediaPlayer == null){
            Toast.makeText(this, "NO HAY CANCION REPRODUCIENDOSE", Toast.LENGTH_SHORT).show();
        } else {
            int cantidadCanciones = Integer.parseInt(sizeCanciones);
            if (posicionActual > 0){
                posicionActual--;
            } else {
                posicionActual = cantidadCanciones - 1;
            }
            reproducirCancion(nombreCancion);
        }

    }

    private void siguienteCancion(String sizeCanciones, String nombreCancion){
        if (mediaPlayer == null){
            Toast.makeText(this, "NO HAY CANCION REPRODUCIENDOSE", Toast.LENGTH_SHORT).show();
        } else {
            int cantidadCanciones =  Integer.parseInt(sizeCanciones);
            if (posicionActual < cantidadCanciones - 1){
                posicionActual++;
            } else {
                posicionActual = 0;
            }
            reproducirCancion(nombreCancion);
        }

    }
    private void stopOrPlay(){

        if (mediaPlayer == null){
            Toast.makeText(this, "NO HAY CANCION REPRODUCIENDOSE", Toast.LENGTH_SHORT).show();
        } else {

            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                posicionActual = mediaPlayer.getCurrentPosition();
            } else {
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(MusicPlayerService.this, encontrarRawUri(cancionActual));
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.seekTo(posicionActual);
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.prepareAsync();

                } catch (IOException e) {
                    e.getMessage();
                }
            }
        }




    }
    private void reproducirCancion(String nombreCancion){

        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(MusicPlayerService.this, encontrarRawUri(nombreCancion));
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try {
                        mediaPlayer.start();
                        cancionActual = nombreCancion;
                        posicionActual = mediaPlayer.getCurrentPosition();
                    }catch (Exception e){
                        Log.e("ERROR", e.getMessage());
                    }
                }
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.d("ERROR", e.getMessage());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            receiver = intent.getParcelableExtra("receiver");
            String action = intent.getAction();
            if (ACTION_PLAY.equals(action)){
                String nombreCancion = intent.getStringExtra("nombreCancion");
                reproducirCancion(nombreCancion);
            } else if (STOP_OR_PLAY.equals(action)) {
                stopOrPlay();
            } else if (SIGUIENTE_CANCION.equals(action)) {
                String cancionesSize = intent.getStringExtra("cancionesSize");
                String nombreCancion = intent.getStringExtra("nombreCancion");
                siguienteCancion(cancionesSize, nombreCancion);
            } else if (ANTERIOR_CANCION.equals(action)) {
                String cancionesSize = intent.getStringExtra("cancionesSize");
                String nombreCancion = intent.getStringExtra("nombreCancion");
                anteriorCancion(cancionesSize, nombreCancion);
            }

            new Thread() {
                @Override
                public void run() {

                    Field[] fields = R.raw.class.getFields();
                    ArrayList<String> nombres = new ArrayList<>();
                    ArrayList<byte[]> fotos = new ArrayList<>();

                    for (Field field : fields){
                        int rawCancionId = getResources().getIdentifier(field.getName(),"raw",getPackageName());
                        if (rawCancionId != 0){
                            cargarData = new MediaMetadataRetriever();
                            Uri rawUri = Uri.parse("android.resource://" + getPackageName() + "/" + rawCancionId);
                            cargarData.setDataSource(MusicPlayerService.this, rawUri);
                            fotos.add(cargarData.getEmbeddedPicture());
                            nombres.add(cargarData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toLowerCase());
                        }
                    }

                    if (receiver != null){
                        Bundle bundleLista = new Bundle();
                        byte[][] fotosFinales = new byte[fotos.size()][];
                        for (int i = 0; i < fotos.size(); i++){
                            fotosFinales[i] = fotos.get(i);
                        }
                        bundleLista.putSerializable("foto" , fotosFinales);
                        bundleLista.putStringArrayList("nombre", nombres);
                        receiver.send(CODE_CANCION, bundleLista);
                    }
                }
            }.start();
        }
        return START_STICKY;
    }

    private Uri encontrarRawUri(String nombreCancion){
        Field field = null;
        try {
            field = R.raw.class.getField(nombreCancion);
            int rawCancionId = getResources().getIdentifier(field.getName(),"raw", getPackageName());
            Uri rawUri = Uri.parse("android.resource://" + getPackageName() + "/" + rawCancionId);
            return rawUri;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }

}
