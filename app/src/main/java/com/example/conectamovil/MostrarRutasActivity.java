package com.example.conectamovil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MostrarRutasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RutaAdapter rutaAdapter;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_rutas);

        // Inicializa laa referencia a Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("ubicaciones")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("rutas");

        // Inicializa el RecyclerView y su adaptador
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rutaAdapter = new RutaAdapter();
        recyclerView.setAdapter(rutaAdapter);

        // Obtén y muestra las rutas desde Firebase
        obtenerRutasDesdeFirebase();

        // Agregar un clic de elemento para abrir HistorialRutasActivity
        rutaAdapter.setOnItemClickListener(new RutaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ruta ruta) {
                // Abre HistorialRutasActivity y pasa la información de la ruta
                Intent intent = new Intent(MostrarRutasActivity.this, HistorialRutasActivity.class);
                intent.putExtra("rutaSeleccionada", ruta);
                startActivity(intent);
            }
        });
    }

    private void obtenerRutasDesdeFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ruta> listaRutas = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ruta ruta = snapshot.getValue(Ruta.class);
                    if (ruta != null) {
                        listaRutas.add(ruta);
                    }
                }

                // Actualiza el adaptador con las rutas obtenidas
                rutaAdapter.setListaRutas(listaRutas);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MostrarRutasActivity.this, "Error al obtener las rutas", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
