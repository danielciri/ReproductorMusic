package com.danielcirilo.reproductormusica;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SeekBar sbProgreso;
    private RecyclerView rvListaCanciones;
    private TextView tvCancionActual;
    private ReproductorService reproductorService;
    private Button bPrevio;
    private Button bReproducir;
    private Button bSiguiente;
    private ArrayList<Song> canciones = new ArrayList<>();
    private boolean playing;
    private Intent intent;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            reproductorService = ((ReproductorService.MusicBinder)service).getService();
            Log.i(TAG, "Conectado" );
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            reproductorService = null;
            Log.i(TAG, "Desconectado");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent =  new Intent(this,ReproductorService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        sbProgreso = findViewById(R.id.sbProgreso);
        bPrevio = findViewById(R.id.bPrevio);
        bReproducir = findViewById(R.id.bReproducir);
        bSiguiente = findViewById(R.id.bSiguiente);
        rvListaCanciones =findViewById(R.id.rvListaCanciones);
        tvCancionActual = findViewById(R.id.tvCancionActual);


        final CancionesAdapter adapter = new CancionesAdapter(canciones);
        rvListaCanciones.setAdapter(adapter);
        rvListaCanciones.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));




        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sbProgreso.setMax(reproductorService.getDuracionCancion()/1000);
                sbProgreso.setProgress(reproductorService.getCurrentPosition()/1000);

            }
        };

        bReproducir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(playing){
                    bReproducir.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                    reproductorService.pauseSong(sbProgreso.getProgress());
                    tvCancionActual.setText(reproductorService.cancionActual().getNombre().toUpperCase());
                    playing= false;

                }else{

                    bReproducir.setBackgroundResource(R.drawable.ic_pause_black_24dp);
                    reproductorService.play();
                    tvCancionActual.setText(reproductorService.cancionActual().getNombre().toUpperCase());
                    canciones = reproductorService.getCanciones();
                    adapter.swap(canciones);

                    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(runnable,0,200, TimeUnit.MILLISECONDS);


                    sbProgreso.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if(fromUser){
                                reproductorService.seekToPos(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }


                    });
                    playing = true;

                }

            }

        });

        bSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playing) {
                    reproductorService.siguiente();
                    tvCancionActual.setText(reproductorService.cancionActual().getNombre().toUpperCase());

                }

            }
        });

        bPrevio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (playing){
                    reproductorService.atras();
                    tvCancionActual.setText(reproductorService.cancionActual().getNombre().toUpperCase());
                }
            }
        });

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bReproducir.setBackgroundResource(R.drawable.ic_pause_black_24dp);

                reproductorService.repSong(canciones.get(rvListaCanciones.getChildAdapterPosition(v)));
                tvCancionActual.setText(reproductorService.cancionActual().getNombre().toUpperCase());

                playing = true;
            }
        });

    }

    @Override
    protected void onDestroy() {
        if(isFinishing()) {
            finishService(reproductorService,serviceConnection);
        }
        super.onDestroy();
    }



    public void finishService(ReproductorService reproductorService, ServiceConnection serviceConnection){

        if(reproductorService != null) {
            unbindService(serviceConnection);
            stopService(new Intent(this, reproductorService.getClass()));
        }

    }


}