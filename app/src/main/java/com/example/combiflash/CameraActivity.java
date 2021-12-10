package com.example.combiflash;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.jjoe64.graphview.GraphView;

public class CameraActivity extends AppCompatActivity {
    private ImageCapture imageCapture;
    private boolean flashMode = false;
    private final int mRequestCode = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private boolean isStartedForResult;
    private int sampleIndex = -1;
    ProgressDialog dialog;
    PreviewView preV;
    File outputDirectory;
    Camera camera;
    ExecutorService cameraExecutor;
    ImageView cameraCaptureBtn, flashBtnOn, flashBtnOff;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        isStartedForResult = getIntent().getBooleanExtra(getResources().getString(R.string.isStartedForResultKey), false);
        if (isStartedForResult) {
            sampleIndex = getIntent().getIntExtra(getResources().getString(R.string.sampleIndexKey), -1);
            activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent backwardIntent = new Intent();
                    // TODO: 10/12/21 put the info received in result to the backward intent
//            intent.putExtra()
                    setResult(RESULT_OK, backwardIntent);
                }
                finish();
            });
        }

        getSupportActionBar().hide();
        dialog = new ProgressDialog(CameraActivity.this);
        // Check camera permissions if all permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]
                    {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            startCamera();
        }
        preV = findViewById(R.id.viewFinder);
        flashBtnOn = findViewById(R.id.flashOn);
        flashBtnOff = findViewById(R.id.flashOff);
        cameraCaptureBtn = findViewById(R.id.camera_capture_button);
        cameraCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Processing...");
                dialog.show();
                takePhoto();
            }
        });
        flashBtnOff.setVisibility(View.VISIBLE);
        flashBtnOn.setVisibility(View.GONE);
        flashBtnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashBtnOff.setVisibility(View.GONE);
                flashBtnOn.setVisibility(View.VISIBLE);
                if (camera.getCameraInfo().hasFlashUnit()) {
                    camera.getCameraControl().enableTorch(true);
                }
            }
        });
        flashBtnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashBtnOn.setVisibility(View.GONE);
                flashBtnOff.setVisibility(View.VISIBLE);
                if (camera.getCameraInfo().hasFlashUnit()) {
                    camera.getCameraControl().enableTorch(false);
                }
            }
        });
        outputDirectory = getOutputDirectory();
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required.", Toast.LENGTH_LONG).show();
                this.finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    preview.setSurfaceProvider(preV.createSurfaceProvider());
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build();
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview, imageCapture);
                } catch (ExecutionException | InterruptedException e) {
                    Toast.makeText(CameraActivity.this, "Error happen", Toast.LENGTH_SHORT).show();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private File getOutputDirectory() {
        File mediaDir = new File(new ContextWrapper(this).getExternalMediaDirs()[0], getResources().getString(R.string.app_name));
        if (!mediaDir.exists()) {
            mediaDir.mkdir();
        }
        if (mediaDir != null) {
            return mediaDir;
        } else {
            return getFilesDir();
        }
    }

    private void takePhoto() {
        if (imageCapture == null) {
            return;
        }
        final File photoFile = new File(outputDirectory,
                new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);

                        // TODO: 08/12/21 if this activity is statrfor result then start the show image activity too for result and set the result after getting the result back from showimage activity in onactivityresult otherwise all good
                        // TODO: 08/12/21 also pass the info whether the showimgactivity is being strarted for result 
                        Intent intent = new Intent(CameraActivity.this, ShowImgActivity.class);
                        intent.putExtra("img_path", String.valueOf(savedUri));
                        Log.e("TAG", "onImageSaved: " + savedUri);
                        Toast.makeText(CameraActivity.this, "Photo capture succeeded", Toast.LENGTH_LONG).show();
                        cameraCaptureBtn.setClickable(false);

                        if (isStartedForResult) {
                            intent.putExtra(getResources().getString(R.string.isStartedForResultKey), true);
                            intent.putExtra(getResources().getString(R.string.sampleIndexKey), sampleIndex);
                            activityResultLauncher.launch(intent);
//                            startActivityForResult(intent, requestCode);
                        } else {
                            intent.putExtra(getResources().getString(R.string.isStartedForResultKey), false);
                            startActivity(intent);
                            finish();
                        }
                        dialog.dismiss();

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraActivity.this, "Photo capture failed" + exception.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dialog.cancel();
    }
}