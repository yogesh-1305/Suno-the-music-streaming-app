package com.suno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.logging.Logger;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    EditText loginUsername;
    EditText loginPassword;
    EditText signUpEmail;
    EditText signUpPassword;
    EditText signUpRetypePassword;

    Button loginButton;
    Button signUpButton;
    ToggleButton toggleButton;

    CardView loginCardView;
    CardView signUpCardView;

    ImageView logo;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            UpdateUI();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mAuth = FirebaseAuth.getInstance();

        logo = findViewById(R.id.appLogo);

        loginUsername = findViewById(R.id.loginUsernameEditText);
        loginPassword = findViewById(R.id.loginPaswordEditText);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        signUpEmail = findViewById(R.id.singUpEmailEditText);
        signUpPassword = findViewById(R.id.singUpPasswordEditText);
        signUpRetypePassword = findViewById(R.id.singUpRetypePasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        loginCardView = findViewById(R.id.logInCardView);
        signUpCardView = findViewById(R.id.signUpCardView);

        toggleButton = findViewById(R.id.loginSignupToggleButton);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Log.i("checked","true");
                    loginCardView.setVisibility(View.INVISIBLE);
                    signUpCardView.setVisibility(View.VISIBLE);
                    logo.setVisibility(View.INVISIBLE);
                }else {
                    Log.i("checked","false");
                    signUpCardView.setVisibility(View.INVISIBLE);
                    loginCardView.setVisibility(View.VISIBLE);
                    logo.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void signIn(){
        String email = loginUsername.getText().toString();
        String password = loginPassword.getText().toString();
        if (email.equals("") || password.equals("")){
            Toast.makeText(this, "Email | Password are Required", Toast.LENGTH_SHORT).show();
        }else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        Toast.makeText(SignUpActivity.this, "Logged in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        UpdateUI();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignUpActivity.this, "Login Error!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void signUp(){
        final String email = signUpEmail.getText().toString();
        final String password = signUpPassword.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FirebaseUser user = mAuth.getCurrentUser();
                assert user != null;
                Toast.makeText(SignUpActivity.this, "Signed Up as: " + email, Toast.LENGTH_SHORT).show();
                uploadUserDetailsToDatabase(email, password);
                UpdateUI();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("sign up", "failed");
            }
        });
    }

    public void uploadUserDetailsToDatabase(String email, String password){
        User user = new User(email, password);
        FirebaseDatabase.getInstance().getReference("Users").push()
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("details", "uploaded");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("details", "upload failed");
            }
        });
    }

    public void UpdateUI(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}