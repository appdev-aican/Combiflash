package com.example.combiflash;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class LinkedDeviceActivity extends AppCompatActivity {
    AppCompatButton linkDev,signOut;
    TextView userName,userEmail;
    ConstraintLayout userDetailMenu;
    LinearLayout outSideLayout,deviceHolderLayout;
    FirebaseFirestore db;
    FirebaseAuth auth;
    DocumentReference user;
    ViewGroup viewGroup;
    Stack<View> viewStack;
    static String staticEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Link a device");
        setContentView(R.layout.activity_linked_device);
        linkDev=findViewById(R.id.linkDev);
        deviceHolderLayout=findViewById(R.id.device_holder);
        viewGroup=(ViewGroup) findViewById(android.R.id.content);
        viewStack=new Stack<>();
        userDetailMenu=findViewById(R.id.userDetailMenu);
        outSideLayout=findViewById(R.id.outLinear);
        userDetailMenu.setVisibility(View.GONE);
        userName=findViewById(R.id.name);
        userEmail=findViewById(R.id.email);
        signOut=findViewById(R.id.signOut);
        db=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        user=db.document("users/"+auth.getCurrentUser().getUid());
        loadData();
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(LinkedDeviceActivity.this,LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
        linkDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LinkedDeviceActivity.this, ScanQRCodeActivity.class);
                startActivity(intent);
            }
        });
        outSideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDetailMenu.setVisibility(View.GONE);
            }
        });
    }
    private void loadData() {
        user.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    while(viewStack.size()>0){
                        View layView=viewStack.peek();
                        deviceHolderLayout.removeView(layView);
                        viewStack.pop();
                    }
                    userName.setText(value.get("name").toString());
                    staticEmail=value.get("email").toString();
                    userEmail.setText(value.get("email").toString());
                    Map<String,Object> map = value.getData();
                    Object[]key= map.keySet().toArray();
                    int valueCount=0;
                    for(int i=0;i<map.size();i++){
                        if((!key[i].toString().equals("name"))&&(!key[i].toString().equals("email"))){
                            valueCount++;
                            View v = LayoutInflater.from(LinkedDeviceActivity.this).inflate(R.layout.device_holder_item, viewGroup, false);
                            viewStack.push(v);
                            deviceHolderLayout.addView(v);
                            CountDownTimer timer;
                            TextView disconnect=v.findViewById(R.id.disconnect);
                            TextView txt=v.findViewById(R.id.txt);
                            TextView timerTxt=v.findViewById(R.id.timerTxt);
                            txt.setText(String.format("Combiflash %d", valueCount));
                            int finalI = i;
                            disconnect.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    user.update(FieldPath.of(key[finalI].toString()),FieldValue.delete())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(LinkedDeviceActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                            timer=new CountDownTimer
                                    ((Long.parseLong(String.valueOf(value.get(FieldPath.of(key[i].toString()))))-System.currentTimeMillis()),
                                            60000) {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void onTick(long l) {
                                    long minTime= TimeUnit.MILLISECONDS.toMinutes(l);
                                    timerTxt.setText(String.format("%d mins remaining", minTime + 1));
                                }
                                @Override
                                public void onFinish() {
                                    user.update(FieldPath.of(key[finalI].toString()),FieldValue.delete());
                                }
                            };
                            timer.start();
                            v.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LinkedDeviceActivity.this);
                                    builder.setTitle("Please select mode");
                                    final View customLayout = getLayoutInflater().inflate(R.layout.mode_select_layout, null);
                                    builder.setView(customLayout);
                                    builder.setPositiveButton("OK", (dialog, which) -> {
                                        RadioGroup radioGroup = customLayout.findViewById(R.id.rgModes);
                                        switch (radioGroup.getCheckedRadioButtonId()) {
                                            case R.id.rbStandard:
                                                startActivity(new Intent(LinkedDeviceActivity.this, LaunchCameraActivity.class));
                                                break;
                                            case R.id.rbDual:
                                                startActivity(new Intent(LinkedDeviceActivity.this, LaunchCameraDualActivity.class));
                                                break;
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            finish();
                                        }
                                    });
                                    dialog.show();
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                    RadioButton radioButtonStandard = customLayout.findViewById(R.id.rbStandard);
                                    RadioButton radioButtonDual = customLayout.findViewById(R.id.rbDual);
                                    radioButtonStandard.setOnClickListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true));
                                    radioButtonDual.setOnClickListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true));
                                }
                            });
                        }
                    }
                }
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
        if(item.getItemId()==R.id.user){
            userDetailMenu.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }
}