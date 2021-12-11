package com.example.combiflash;

import static com.example.combiflash.AppFunctions.setTextDrawables;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LaunchCameraDualActivity extends AppCompatActivity {
    boolean isFirstSampleTaken = false, isSecondSampleTaken = false;
    ConstraintLayout userDetailMenu, restLayout;
    AppCompatButton startCam1, startCam2, signOut, proceedBtn;
    TextView userName, userEmail, tvSample1, tvSample2;
    FirebaseFirestore db;
    FirebaseAuth auth;
    DocumentReference user;
    ActivityResultLauncher<Intent> activityResultLauncher;
SampleData[] sampleData=new SampleData[2];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_camera_dual);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("CombiFlash (Dual)");
        tvSample1 = findViewById(R.id.tvSample1);
        tvSample2 = findViewById(R.id.tvSample2);
        proceedBtn = findViewById(R.id.btnProceed);
        startCam1 = findViewById(R.id.btnRecSampleA);
        startCam2 = findViewById(R.id.btnRecSampleB);
        userDetailMenu = findViewById(R.id.userDetailMenu);
        restLayout = findViewById(R.id.restLayout);
        userDetailMenu.setVisibility(View.GONE);
        userName = findViewById(R.id.name);
        userEmail = findViewById(R.id.email);
        signOut = findViewById(R.id.signOut);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = db.document("users/" + auth.getCurrentUser().getUid());
        loadData();
        proceedBtn.setVisibility(View.INVISIBLE);
        proceedBtn.setOnClickListener(v -> {
            Log.e("TAG", "launchcamera dual: put conc values "+sampleData[0].getConcentration()+" "+sampleData[1].getConcentration() );
           Intent graphIntent=new Intent(LaunchCameraDualActivity.this,GraphActivity.class);
            graphIntent.putExtra(getResources().getString(R.string.modeKey),getResources().getString(R.string.modeDual));
            graphIntent.putExtra(getResources().getString(R.string.concentration1Key),sampleData[0].getConcentration());
            graphIntent.putExtra(getResources().getString(R.string.concentration2Key),sampleData[1].getConcentration());
            graphIntent.putExtra(getResources().getString(R.string.rfArray1Key),sampleData[0].getRfValues());
            graphIntent.putExtra(getResources().getString(R.string.rfArray2Key),sampleData[1].getRfValues());
            startActivity(graphIntent);
        });
        signOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(LaunchCameraDualActivity.this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
        restLayout.setOnClickListener(view -> userDetailMenu.setVisibility(View.GONE));
        userDetailMenu.setOnClickListener(view -> userDetailMenu.setVisibility(View.GONE));
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    int si = result.getData().getIntExtra(getResources().getString(R.string.sampleIndexKey), -1);
                    switch (si) {
                        case 1:
                            setTextDrawables(tvSample1, 0, 0, 0, 0, R.drawable.ic_baseline_check_24, R.color.green, 0, 0);
                            startCam1.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_background_blue, null));
                            startCam1.setText("Retake");
                            isFirstSampleTaken = true;
                            sampleData[0]=new SampleData(result.getData().getFloatExtra(getResources().getString(R.string.concentrationKey),-1),result.getData().getFloatArrayExtra(getResources().getString(R.string.rfArrayKey)));
                            Log.e("TAG", "launchcamera dual: got conc values "+sampleData[0].getConcentration() );

                            break;
                        case 2:
                            setTextDrawables(tvSample2, 0, 0, 0, 0, R.drawable.ic_baseline_check_24, R.color.green, 0, 0);
                            startCam2.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_background_blue, null));
                            startCam2.setText("Retake");
                            isSecondSampleTaken = true;
                            sampleData[1]=new SampleData(result.getData().getFloatExtra(getResources().getString(R.string.concentrationKey),-1),result.getData().getFloatArrayExtra(getResources().getString(R.string.rfArrayKey)));
                            Log.e("TAG", "launchcamera dual: got conc values "+sampleData[1].getConcentration() );
                            break;
                        default:
                            Toast.makeText(LaunchCameraDualActivity.this, "Sample couldn't be recorded", Toast.LENGTH_SHORT).show();
                    }
                }
                if (isFirstSampleTaken && isSecondSampleTaken) {
                    proceedBtn.setVisibility(View.VISIBLE);
                }
            }
        });
        startCam1.setOnClickListener(view -> {
            Intent intent = new Intent(LaunchCameraDualActivity.this, CameraActivity.class);
            intent.putExtra(getResources().getString(R.string.isStartedForResultKey), true);
            intent.putExtra(getResources().getString(R.string.sampleIndexKey), 1);
            activityResultLauncher.launch(intent);
        });
        startCam2.setOnClickListener(view -> {
            Intent intent = new Intent(LaunchCameraDualActivity.this, CameraActivity.class);
            intent.putExtra(getResources().getString(R.string.isStartedForResultKey), true);
            intent.putExtra(getResources().getString(R.string.sampleIndexKey), 2);
            activityResultLauncher.launch(intent);
        });
    }

    private void loadData() {
        user.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        userName.setText(name);
                        userEmail.setText(email);
                    } else {
                        Toast.makeText(LaunchCameraDualActivity.this, "Something wrong", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(LaunchCameraDualActivity.this, "Error!!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.user) {
            userDetailMenu.setVisibility(View.VISIBLE);
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}