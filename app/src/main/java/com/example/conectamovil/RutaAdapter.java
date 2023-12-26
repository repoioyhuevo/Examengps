package com.example.conectamovil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RutaAdapter extends RecyclerView.Adapter<RutaAdapter.RutaViewHolder> {

    private List<Ruta> listaRutas;
    private OnItemClickListener onItemClickListener;

    public void setListaRutas(List<Ruta> listaRutas) {
        this.listaRutas = listaRutas;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Ruta ruta);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RutaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ruta, parent, false);
        return new RutaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaViewHolder holder, int position) {
        Ruta ruta = listaRutas.get(position);

        holder.textViewDireccionInicio.setText("Inicio: " + ruta.getDireccionInicio());
        holder.textViewDireccionFin.setText("Fin: " + ruta.getDireccionFin());
        holder.textViewDistancia.setText("Distancia: " + ruta.getDistancia());
        holder.textViewDuracion.setText("Duración: " + ruta.getDuracion());

        // Otros elementos de interfaz o lógica de manejo

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(ruta);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaRutas != null ? listaRutas.size() : 0;
    }

    static class RutaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDireccionInicio;
        TextView textViewDireccionFin;
        TextView textViewDistancia;
        TextView textViewDuracion;

        public RutaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDireccionInicio = itemView.findViewById(R.id.textViewDireccionInicio);
            textViewDireccionFin = itemView.findViewById(R.id.textViewDireccionFin);
            textViewDistancia = itemView.findViewById(R.id.textViewDistancia);
            textViewDuracion = itemView.findViewById(R.id.textViewDuracion);
        }
    }
}
