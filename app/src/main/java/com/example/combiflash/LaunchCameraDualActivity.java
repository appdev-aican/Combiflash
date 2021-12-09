package com.example.combiflash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LaunchCameraDualActivity extends AppCompatActivity {
    ConstraintLayout userDetailMenu,restLayout;
    AppCompatButton startCam1,startCam2,signOut;
    TextView userName,userEmail;
    FirebaseFirestore db;
    FirebaseAuth auth;
    DocumentReference user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_camera_dual);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("CombiFlash (Dual)");
        startCam1=findViewById(R.id.btnRecSampleA);
        startCam2=findViewById(R.id.btnRecSampleB);
        userDetailMenu=findViewById(R.id.userDetailMenu);
        restLayout=findViewById(R.id.restLayout);
        userDetailMenu.setVisibility(View.GONE);
        userName=findViewById(R.id.name);
        userEmail=findViewById(R.id.email);
        signOut=findViewById(R.id.signOut);
        db= FirebaseFirestore.getInstance();
        auth= FirebaseAuth.getInstance();
        user=db.document("users/"+auth.getCurrentUser().getUid());
        loadData();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(LaunchCameraDualActivity.this,LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
        restLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDetailMenu.setVisibility(View.GONE);
            }
        });
        userDetailMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDetailMenu.setVisibility(View.GONE);
            }
        });

        startCam1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 08/12/21 pass the info key that CameraActivity is being started as not startforactivity
                startActivity(new Intent(LaunchCameraDualActivity.this, CameraActivity.class));
            }
        });
        startCam2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 08/12/21 pass the info key that CameraActivity is being started as not startforactivity
                startActivity(new Intent(LaunchCameraDualActivity.this, CameraActivity.class));
            }
        });
    }
    private void loadData() {
        user.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String name=documentSnapshot.getString("name");
                            String email=documentSnapshot.getString("email");
                            userName.setText(name);
                            userEmail.setText(email);
                        }
                        else{
                            Toast.makeText(LaunchCameraDualActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LaunchCameraDualActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.user) {
            userDetailMenu.setVisibility(View.VISIBLE);
        } else if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}