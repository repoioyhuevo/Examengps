package com.example.conectamovil;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    EditText mEditTextNombre;
    EditText mEditTextApellido;
    EditText mEditTextEmail;
    EditText mEditTextPass;
    EditText mEditTextConfirmarPass;
    EditText mEditTextUsuario;
    Button mButtonRegistrar;
    TextView mTextViewRespuestaR;
    CheckBox rememberEmailCheckbox;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mEditTextNombre = findViewById(R.id.editTextNombre);
        mEditTextApellido = findViewById(R.id.editTextApellido);
        mEditTextEmail = findViewById(R.id.editTextEmail);
        mEditTextPass = findViewById(R.id.editTextPass);
        mEditTextConfirmarPass = findViewById(R.id.editConfirmarPass);
        mEditTextUsuario = findViewById(R.id.editTextUsuario);
        mButtonRegistrar = findViewById(R.id.btnRegistrar);
        mTextViewRespuestaR = findViewById(R.id.textViewRespuestaR);
        rememberEmailCheckbox = findViewById(R.id.rememberEmailCheckbox);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("usuarios");

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        String savedEmail = sharedPreferences.getString("email", "");
        mEditTextEmail.setText(savedEmail);

        mButtonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = mEditTextNombre.getText().toString();
                String apellido = mEditTextApellido.getText().toString();
                String email = mEditTextEmail.getText().toString();
                String password = mEditTextPass.getText().toString();
                String usuario = mEditTextUsuario.getText().toString();

                if (nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() || !emailValido(email) || password.length() < 6) {
                    Toast.makeText(RegistroActivity.this, "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistroActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (rememberEmailCheckbox.isChecked()) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("email", email);
                                        editor.apply();
                                    } else {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.remove("email");
                                        editor.apply();
                                    }

                                    Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid();
                                        // Resto del código...
                                    }
                                } else {
                                    Toast.makeText(RegistroActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    mTextViewRespuestaR.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                }
                            }
                        });
            }
        });
    }

    private boolean emailValido(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
