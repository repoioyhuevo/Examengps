package com.example.conectamovil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PerfilActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText mEditTextNombrePerfil;
    private EditText mEditTextApellidoPerfil;
    private EditText mEditTextApodoPerfil;  // Nuevo campo para el apodo
    private Button mBtnGuardarPerfil;
    private ImageView mImageViewFotoPerfil;
    private Button mBtnSeleccionarFoto;

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference userRef;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);
        }

        mEditTextNombrePerfil = findViewById(R.id.editTextNombrePerfil);
        mEditTextApellidoPerfil = findViewById(R.id.editTextApellidoPerfil);
        mEditTextApodoPerfil = findViewById(R.id.editTextApodoPerfil);  // Nuevo campo para el apodo
        mBtnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);
        mImageViewFotoPerfil = findViewById(R.id.imageViewFotoPerfil);
        mBtnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);

        // Load default image from drawable
        mImageViewFotoPerfil.setImageResource(R.drawable.ic_launcher_foreground);

        mBtnGuardarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarPerfil();
            }
        });

        mBtnSeleccionarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarFoto();
            }
        });

        cargarDatosUsuario();

        Button btnIrOtraActividad = findViewById(R.id.btnMostrarDatos);

        // Configurar el evento click del botón
        btnIrOtraActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para ir a la otra actividad
                Intent intent = new Intent(PerfilActivity.this, MostrarDatosActivity.class);

                // Iniciar la otra actividad
                startActivity(intent);
            }
        });



    }

    private void cargarDatosUsuario() {
        if (userRef != null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            mEditTextNombrePerfil.setText(usuario.getNombre());
                            mEditTextApellidoPerfil.setText(usuario.getApellido());
                            mEditTextApodoPerfil.setText(usuario.getUsuario());  // Cargar apodo

                            // Cargar foto desde Firebase Realtime Database
                            if (usuario.getImageUrl() != null && !usuario.getImageUrl().isEmpty()) {
                                Picasso.get().load(usuario.getImageUrl()).into(mImageViewFotoPerfil);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PerfilActivity.this, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void guardarPerfil() {
        if (userRef != null) {
            String nuevoNombre = mEditTextNombrePerfil.getText().toString();
            String nuevoApellido = mEditTextApellidoPerfil.getText().toString();
            String nuevoApodo = mEditTextApodoPerfil.getText().toString();  // Obtener el nuevo apodo

            userRef.child("nombre").setValue(nuevoNombre);
            userRef.child("apellido").setValue(nuevoApellido);
            userRef.child("usuario").setValue(nuevoApodo);  // Actualizar el apodo

            Toast.makeText(PerfilActivity.this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
        }
    }

    private void seleccionarFoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                mImageViewFotoPerfil.setImageBitmap(bitmap);
                subirFotoFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void subirFotoFirebase() {
        if (imageUri != null && userRef != null) {
            StorageReference ref = storageReference.child("fotos_perfil/" + mAuth.getCurrentUser().getUid());

            mImageViewFotoPerfil.setDrawingCacheEnabled(true);
            mImageViewFotoPerfil.buildDrawingCache();
            Bitmap bitmap = mImageViewFotoPerfil.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            ref.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Una vez que la foto se suba con éxito, obtener la URL de la foto y actualizar en Realtime Database
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            userRef.child("imageUrl").setValue(uri.toString());
                            Toast.makeText(PerfilActivity.this, "Foto de perfil subida con éxito", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PerfilActivity.this, "Error al subir la foto de perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
