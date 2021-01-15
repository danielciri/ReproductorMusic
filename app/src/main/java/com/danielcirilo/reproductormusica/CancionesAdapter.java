package com.danielcirilo.reproductormusica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CancionesAdapter extends RecyclerView.Adapter<CancionesAdapter.SongsHolder> implements View.OnClickListener
{
    private ArrayList<Song> listaSongs;
    private View.OnClickListener listener;

    public CancionesAdapter(ArrayList<Song> listaSongs) {
        this.listaSongs = listaSongs;
    }

    @NonNull
    @Override
    public SongsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.cancion_item,parent,false);
        item.setOnClickListener(this);
        return new SongsHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsHolder holder, int position) {
        Song song = listaSongs.get(position);
        holder.bindCancion(song);

    }

    @Override
    public int getItemCount() {
        return listaSongs.size();
    }
    public void swap(ArrayList<Song> canciones) {
        this.listaSongs = canciones;
        notifyDataSetChanged();
    }
    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener!=null){
            listener.onClick(v);
        }

    }

    public class SongsHolder extends RecyclerView.ViewHolder{
        private TextView tvNombreCancion;

        public SongsHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCancion = itemView.findViewById(R.id.tvNombreCancion);

        }

        public void bindCancion(Song song){
            tvNombreCancion.setText(song.getNombre().toUpperCase());
        }


    }
}