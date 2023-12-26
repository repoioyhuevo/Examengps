package com.example.conectamovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MostrarDatosActivity extends AppCompatActivity {

    private TextView mTextViewNombre;
    private TextView mTextViewApellido;
    private TextView mTextViewUsuario;
    private TextView mTextViewEmail;
    private ImageView mImageViewFotoPerfil;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_datos);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);
        }

        mTextViewNombre = findViewById(R.id.textViewNombre);
        mTextViewApellido = findViewById(R.id.textViewApellido);
        mTextViewUsuario = findViewById(R.id.textViewUsuario);
        mTextViewEmail = findViewById(R.id.textViewEmail);
        mImageViewFotoPerfil = findViewById(R.id.imageViewFotoPerfil);

        mostrarDatosUsuario();

        Button btnIrAPerfil = findViewById(R.id.btnIrAPerfil);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnIrAPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MostrarDatosActivity.this, PerfilActivity.class);
                startActivity(intent);
            }
        });

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
    }

    private void mostrarDatosUsuario() {
        if (userRef != null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            mTextViewNombre.setText("Nombre: " + usuario.getNombre());
                            mTextViewApellido.setText("Apellido: " + usuario.getApellido());
                            mTextViewUsuario.setText("Usuario: " + usuario.getUsuario());
                            mTextViewEmail.setText("Email: " + usuario.getEmail());

                            if (usuario.getImageUrl() != null && !usuario.getImageUrl().isEmpty()) {
                                Picasso.get().load(usuario.getImageUrl()).into(mImageViewFotoPerfil);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MostrarDatosActivity.this, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MostrarDatosActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
