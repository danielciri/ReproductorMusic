package com.danielcirilo.reproductormusica;


import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ReproductorService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "ReproductorService";
    private ArrayList<Song> canciones;
    private final IBinder mBinder= new MusicBinder();
    private MediaPlayer reproductor;
    private int posicionCancion;
    private int progress;
    private Song songActual;

    public ReproductorService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
        canciones = addCanciones();
        posicionCancion = 0;
        progress=0;
        reproductor = MediaPlayer.create(ReproductorService.this,canciones.get(posicionCancion).getId());
        reproductor.setOnCompletionListener(ReproductorService.this);
        reproductor.setOnPreparedListener(ReproductorService.this);
        reproductor.setOnErrorListener(ReproductorService.this);
        songActual = new Song();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reproductor.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK); // Para mantener la pantalla encendida
        return START_STICKY;
    }


    public void seekToPos(int msec){
        reproductor.seekTo(msec*1000);
    }


    public void play() {

        AssetFileDescriptor afd = this.getResources().openRawResourceFd(canciones.get(posicionCancion).getId());

        try {

            reproductor.stop();
            reproductor.reset();
            reproductor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            reproductor.prepare();
            if (progress!=0){

                reproductor.seekTo(progress);
            }

            afd.close();
            reproductor.start();
            songActual = canciones.get(posicionCancion);

        }
        catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException: " + e.getMessage());
        }
        catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException: " + e.getMessage());
        }
        catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }


    public ArrayList<Song> getCanciones() {
        return canciones;
    }


    public int getDuracionCancion(){
        return reproductor.getDuration();
    }


    public int getCurrentPosition(){
        return reproductor.getCurrentPosition();
    }

    public void siguiente(){
        posicionCancion++;
        if (posicionCancion == canciones.size()) posicionCancion = 0;
        play();
    }
    public void atras(){
        posicionCancion--;
        if (posicionCancion < 0){
            posicionCancion = canciones.size() - 1;
        }
        play();
    }

    public void pauseSong(int progress){
        reproductor.pause();
        this.progress = progress *1000;

    }
    public Song cancionActual(){
        return songActual;
    }


    public void repSong(Song song){
        for (Song c : canciones) {
            if (c.getId() == song.getId()) {
                posicionCancion = canciones.indexOf(c);
            }
        }
        progress = 0;
        play();
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }


    private ArrayList<Song> addCanciones(){
        Field[] fields = R.raw.class.getFields();
        MediaPlayer mp;
        ArrayList<Song> songs = new ArrayList<>();
        int idCancion=0;
        for (int i =0; i < fields.length; i++) {
            try {
                idCancion = fields[i].getInt(fields[i]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            mp = MediaPlayer.create(this,idCancion);
            songs.add(new Song(idCancion,getResources().getResourceEntryName(idCancion),mp.getDuration()));
        }
        return songs;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        posicionCancion++;
        if(posicionCancion == 0){
            posicionCancion = canciones.size()-1;
        }else  if (posicionCancion == canciones.size()){
            posicionCancion = 0;
        }
        play();
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        play();
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG,"onPrepared");

    }

    public class MusicBinder extends Binder {
        public ReproductorService getService(){
            return ReproductorService.this;
        }
    }
}