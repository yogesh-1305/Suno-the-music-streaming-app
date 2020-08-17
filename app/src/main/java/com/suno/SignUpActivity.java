package com.suno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
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
    ToggleButton logInCardShowPassword;
    ToggleButton signUpCardShowPassword;

    Button signInButton;

    CardView loginCardView;
    CardView signUpCardView;

    ImageView logo;
    GoogleSignInClient googleSignInClient;

    ProgressDialog progressDialog;
    CallbackManager mCallbackManager;


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
        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        FacebookSdk.sdkInitialize(SignUpActivity.this);

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        Button facebookLoginButton = findViewById(R.id.facebookLoginButton);
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(SignUpActivity.this, Arrays.asList("email", "public profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
//                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
//                Log.d(TAG, "facebook:onCancel");
                        // ...
                    }

                    @Override
                    public void onError(FacebookException error) {
//                Log.d(TAG, "facebook:onError", error);
                        // ...
                    }
                });
            }
        });

        logo = findViewById(R.id.appLogo);
        loginUsername = findViewById(R.id.loginUsernameEditText);
        loginPassword = findViewById(R.id.loginPaswordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpEmail = findViewById(R.id.singUpEmailEditText);
        signUpPassword = findViewById(R.id.singUpPasswordEditText);
        signUpRetypePassword = findViewById(R.id.singUpRetypePasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        loginCardView = findViewById(R.id.logInCardView);
        signUpCardView = findViewById(R.id.signUpCardView);
        logInCardShowPassword = findViewById(R.id.loginPasswordShowButton);
        signUpCardShowPassword = findViewById(R.id.signupPasswordShowButton);
        toggleButton = findViewById(R.id.loginSignupToggleButton);

        // GOOGLE SIGN IN BUTTON
        signInButton = findViewById(R.id.googleSignInButton);

        // GOOGLE SIGN IN OPTIONS TO GET THE USERDATA
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);

        // GOOGLE SIGN IN BUTTON'S CLICK LISTENER
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
       // EMAIL LOGIN BUTTON'S CLICK LISTENER
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        // EMAIL SIGN UP BUTTON'S CLICK LISTENER
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        // TOGGLE BUTTON
        logInCardShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b){
                    loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    loginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        // SHOW OR HIDE PASSWORD BUTTON IN SIGN UP CARD VIEW
        signUpCardShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b){
                    signUpPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    signUpPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        // SHOW OR HIDE PASSWORD BUTTON IN SIGN IN CARD VIEW
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Log.i("checked","true");
                    loginCardView.setVisibility(View.INVISIBLE);
                    signUpCardView.setVisibility(View.VISIBLE);
                }else {
                    Log.i("checked","false");
                    signUpCardView.setVisibility(View.INVISIBLE);
                    loginCardView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    // EMAIL SIGN IN
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
                    Toast.makeText(SignUpActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // EMAIL SIGN UP
    public void signUp(){
        final String email = signUpEmail.getText().toString();
        final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        final String password = signUpPassword.getText().toString();
        final String retypePassword = signUpRetypePassword.getText().toString();
        if (email.equals("")){
            Toast.makeText(this, "Email address required!", Toast.LENGTH_SHORT).show();
        }else if (!email.matches(emailPattern)){
            Toast.makeText(this, "Please Enter a Valid Email!", Toast.LENGTH_SHORT).show();
        }else if (password.equals("")){
            Toast.makeText(this, "Password required!", Toast.LENGTH_SHORT).show();
        }else if (!password.equals(retypePassword)){
            Toast.makeText(this, "Passwords did not matched!", Toast.LENGTH_SHORT).show();
        }else {
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
    }

    // GOOGLE SIGN IN
    private void signInWithGoogle() {
        Log.i("google sign in", "clicked");
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }

    //  FACEBOOK SIGN IN
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            Toast.makeText(SignUpActivity.this, "Signed in as: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            UpdateUI();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    // RESULT STORED HERE AFTER FETCHING FROM GOOGLE SIGN IN
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            progressDialog.show();
            progressDialog.setMessage("Logging you in...");
            if (requestCode == 9001 && resultCode == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    assert account != null;
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            Toast.makeText(SignUpActivity.this, "Signed in as: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            UpdateUI();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        startActivity(intent);
        finish();
    }
}