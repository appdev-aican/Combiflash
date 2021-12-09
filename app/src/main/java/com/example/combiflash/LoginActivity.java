package com.example.combiflash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    AppCompatButton loginBtn;
    TextInputEditText email,pass;
    TextView registerTxt;
    ImageView googleBtn;
    private GoogleApiClient mGoogleSignInClient;
    private static final int RC_SIGN_IN=1;
    ProgressDialog dialog;
    FirebaseFirestore db;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        loginBtn=findViewById(R.id.loginBtn);
        email=findViewById(R.id.editEmail);
        pass=findViewById(R.id.editPassword);
        registerTxt=findViewById(R.id.registerTxt);
        googleBtn=findViewById(R.id.google);
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if(fAuth.getCurrentUser() != null ){
            startActivity(new Intent(getApplicationContext(),LinkedDeviceActivity.class));
            finish();
        }
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().toString().trim().isEmpty()||pass.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
                else{
                    dialog=new ProgressDialog(LoginActivity.this);
                    dialog.setMessage("Please wait...");
                    dialog.show();
                    fAuth.signInWithEmailAndPassword(email.getText().toString().trim(),
                            pass.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                dialog.dismiss();
                                startActivity(new Intent(LoginActivity.this,LinkedDeviceActivity.class));
                                Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else{
                                dialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Something wrong ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        registerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(LoginActivity.this, "Google Signin Failed....", Toast.LENGTH_SHORT).show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result= Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                dialog=new ProgressDialog(LoginActivity.this);
                dialog.setMessage("Processing...");
                dialog.show();
            }
            else {
                Toast.makeText(this, "Can't get the Auth Result", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = db.collection("users").document(fAuth.getCurrentUser().getUid());

                            Map<String,Object> user = new HashMap<>();
                            user.put("name",idToken.getDisplayName());
                            user.put("email",idToken.getEmail());
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("TAG", "onSuccess: user Profile is created for ");
                                    dialog.dismiss();
                                    startActivity(new Intent(LoginActivity.this,LinkedDeviceActivity.class));
                                    Toast.makeText(LoginActivity.this, "Google signin successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Log.d("TAG", "onFailure: " + e.toString());
                                    Toast.makeText(LoginActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            String e=task.getException().getMessage();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Failed....   "+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}