package com.luiscortes.reproductor;

import android.content.Context;
import android.content.res.Resources;
import android.print.PageRange;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CancionAdapter extends RecyclerView.Adapter<CancionAdapter.CancionesViewHolder> {
    private final ArrayList<Cancion> canciones;

    private IOnClickListener listener;

    public interface IOnClickListener {
        void onItemClick(int position);
    }

    public CancionAdapter(ArrayList<Cancion> canciones, IOnClickListener listener) {
        this.canciones = canciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CancionAdapter.CancionesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View layout = inflater.inflate(R.layout.layout_cancion, parent, false);
        return new CancionesViewHolder(layout, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CancionAdapter.CancionesViewHolder holder, int position) {
        holder.bindCancion(canciones.get(position), position);
    }

    @Override
    public int getItemCount() {
        return canciones.size();
    }

    static class CancionesViewHolder extends RecyclerView.ViewHolder{
        private final TextView tvNombreCV;
        private final ImageView ivFoto;
        private final CardView cvCancion;
        private final IOnClickListener listener;

        public CancionesViewHolder(@NonNull View itemView, IOnClickListener listener) {
            super(itemView);
            this.tvNombreCV = itemView.findViewById(R.id.tvCanionCV);
            this.ivFoto = itemView.findViewById(R.id.ivCancionCV);
            this.cvCancion = itemView.findViewById(R.id.cvCancion);
            this.listener = listener;

            cvCancion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        listener.onItemClick(position);
                    }
                }
            });
        }

        public void bindCancion (Cancion cancion, int position){
            ivFoto.setImageBitmap(cancion.getBitmap(position));
            tvNombreCV.setText(cancion.getNombre());
        }
    }
}
