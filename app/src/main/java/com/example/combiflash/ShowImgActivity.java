package com.example.combiflash;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ShowImgActivity extends AppCompatActivity implements View.OnTouchListener {
    private boolean isStartedForResult;
    private int sampleIndex = -1;
    ImageView iv_capture;
    View viewLineUp;
    ConstraintLayout layout;
    AppCompatButton setConcentrationBtn, alterConcBtn, proceedBtn;
    ActivityResultLauncher<Intent> activityResultLauncher;
    float dy, valueMovement;
    float concentration = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_img);
        getSupportActionBar().hide();
        isStartedForResult = getIntent().getBooleanExtra(getResources().getString(R.string.isStartedForResultKey), false);
        if (isStartedForResult) {
            sampleIndex = getIntent().getIntExtra(getResources().getString(R.string.sampleIndexKey), -1);
            activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Intent backwardIntent = new Intent();
                        backwardIntent.putExtra(getResources().getString(R.string.sampleIndexKey),result.getData().getIntExtra(getResources().getString(R.string.sampleIndexKey), -1));
                        backwardIntent.putExtra(getResources().getString(R.string.concentrationKey),result.getData().getFloatExtra(getResources().getString(R.string.concentrationKey), -1));
                        backwardIntent.putExtra(getResources().getString(R.string.rfArrayKey),result.getData().getFloatArrayExtra(getResources().getString(R.string.rfArrayKey)));

                        setResult(RESULT_OK, backwardIntent);
                    }
                }
                finish();
            });
        }
        String path = getIntent().getStringExtra("img_path");
        iv_capture = findViewById(R.id.iv_capture);
        setConcentrationBtn = findViewById(R.id.setConcentrationBtn);
        alterConcBtn = findViewById(R.id.alterLengthBtn);
        proceedBtn = findViewById(R.id.proceedBtn);
        layout = findViewById(R.id.layout);
        iv_capture.setImageURI(Uri.parse(path));
        viewLineUp = findViewById(R.id.viewLineUp);
        setConcentrationBtn.setVisibility(View.VISIBLE);
        alterConcBtn.setVisibility(View.GONE);
        proceedBtn.setVisibility(View.GONE);
        setConcentrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowImgActivity.this);
                builder.setTitle("Please enter the concentration of B");
                final View customLayout = getLayoutInflater().inflate(R.layout.edit_text_layout, null);
                builder.setView(customLayout);
                builder
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                EditText editText = customLayout.findViewById(R.id.editText);
                                if (editText.getText().toString().isEmpty()) {
                                    Toast.makeText(ShowImgActivity.this, "Please enter any data", Toast.LENGTH_SHORT).show();
                                } else {
                                    concentration = Float.parseFloat(editText.getText().toString());
                                    alterConcBtn.setVisibility(View.VISIBLE);
                                    setConcentrationBtn.setVisibility(View.GONE);
                                    proceedBtn.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        alterConcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowImgActivity.this);
                builder.setTitle("Please enter the concentration of B");
                final View customLayout = getLayoutInflater().inflate(R.layout.edit_text_layout, null);
                builder.setView(customLayout);
                builder
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                EditText editText = customLayout.findViewById(R.id.editText);
                                if (editText.getText().toString().isEmpty()) {
                                    Toast.makeText(ShowImgActivity.this, "Please enter any data", Toast.LENGTH_SHORT).show();
                                } else {
                                    concentration = Float.parseFloat(editText.getText().toString());
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] h = new int[2];
                layout.getLocationOnScreen(h);
                Log.e("pos1", String.valueOf(h[1]));
                Intent intent = new Intent(ShowImgActivity.this, ImageEvaluate.class);
                intent.putExtra("img_path", path);
                intent.putExtra(getResources().getString(R.string.concentrationKey), concentration);
                intent.putExtra("valueUpMove", valueMovement);
                intent.putExtra("pos", h[1]);
                if (isStartedForResult) {
                    intent.putExtra(getResources().getString(R.string.isStartedForResultKey), true);
                    intent.putExtra(getResources().getString(R.string.sampleIndexKey), sampleIndex);
                    activityResultLauncher.launch(intent);
                } else {
                    intent.putExtra(getResources().getString(R.string.isStartedForResultKey), false);
                    startActivity(intent);
                    finish();
                }
            }
        });
        layout.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == layout.getId()) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dy = view.getY() - motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.setY(motionEvent.getRawY() + dy);
                    valueMovement = motionEvent.getRawY() + dy;
                    break;
                default:
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}