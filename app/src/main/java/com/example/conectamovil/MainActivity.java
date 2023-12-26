package com.example.conectamovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText mEditTextEmailLogin;
    private EditText mEditTextPassLogin;
    private Button mBtnIniciarSesion;
    private TextView mTextViewRespuestaLogin;
    private TextView mRegistroRedirectText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextEmailLogin = findViewById(R.id.editTextEmailLogin);
        mEditTextPassLogin = findViewById(R.id.editTextPassLogin);
        mBtnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        mTextViewRespuestaLogin = findViewById(R.id.textViewRespuestaLogin);
        mRegistroRedirectText = findViewById(R.id.registroRedirectText);

        mAuth = FirebaseAuth.getInstance();

        // Verificar si hay un usuario autenticado al abrir la aplicación
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Ya hay un usuario autenticado, redirigir a la actividad principal
            Intent intent = new Intent(MainActivity.this, Principal.class);
            startActivity(intent);
            finish(); // Esto evita que el usuario pueda regresar a la pantalla de inicio de sesión presionando el botón Atrás
        }

        mBtnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEditTextEmailLogin.getText().toString();
                String password = mEditTextPassLogin.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Inicio de sesión exitoso
                                    Toast.makeText(MainActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                                    mTextViewRespuestaLogin.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                                    // Redirigir a la actividad principal
                                    Intent intent = new Intent(MainActivity.this, Principal.class);
                                    startActivity(intent);
                                    finish(); // Esto evita que el usuario pueda regresar a la pantalla de inicio de sesión presionando el botón Atrás
                                } else {
                                    // Si el inicio de sesión falla, muestra un mensaje al usuario
                                    Toast.makeText(MainActivity.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    mTextViewRespuestaLogin.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                }
                            }
                        });
            }
        });

        mRegistroRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Evitar que al presionar el botón Atrás vuelva a la actividad principal después de cerrar sesión
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
