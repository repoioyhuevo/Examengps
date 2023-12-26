package com.example.conectamovil;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HistorialRutasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_rutas);

        // Recuperar datos de la ruta seleccionada
        Ruta rutaSeleccionada = getIntent().getParcelableExtra("rutaSeleccionada");

        // Actualizar la interfaz de usuario con la información de la ruta seleccionada
        mostrarInformacionRutaSeleccionada(rutaSeleccionada);
    }

    private void mostrarInformacionRutaSeleccionada(Ruta ruta) {
        if (ruta != null) {
            // Actualizar vistas con la información de la ruta
            TextView textViewDireccionInicio = findViewById(R.id.textViewDireccionInicio);
            TextView textViewDireccionFin = findViewById(R.id.textViewDireccionFin);
            TextView textViewDistancia = findViewById(R.id.textViewDistancia);
            TextView textViewDuracion = findViewById(R.id.textViewDuracion);
            TextView textViewHoraInicio = findViewById(R.id.textViewHoraInicio);

            textViewDireccionInicio.setText("Inicio: " + (ruta.getDireccionInicio() != null ? ruta.getDireccionInicio() : ""));
            textViewDireccionFin.setText("Fin: " + (ruta.getDireccionFin() != null ? ruta.getDireccionFin() : ""));
            textViewDistancia.setText("Distancia: " + (ruta.getDistancia() != null ? ruta.getDistancia() : ""));
            textViewDuracion.setText("Duración: " + (ruta.getDuracion() != null ? ruta.getDuracion() : ""));
            textViewHoraInicio.setText("Hora de inicio: " + (ruta.getHoraInicio() != null ? ruta.getHoraInicio() : ""));
        } else {

        }
    }

}
