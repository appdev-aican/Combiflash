package com.example.combiflash;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.combiflash.LinkedDeviceActivity.staticEmail;

interface OnQrResultListener {
    void onQrResult(String result);
}

public class ScanQRCodeActivity extends AppCompatActivity implements OnQrResultListener {
    private static final int RC_CAMERA_PERMISSION = 100;
    private static final String[] permissions = new String[]{
            Manifest.permission.CAMERA
    };
    PreviewView previewView;
    ImageAnalysis analyzer;
    ProcessCameraProvider cameraProvider;
    ImageView iv;
    private ExecutorService cameraExecutor;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float ratio = (float) (dm.heightPixels) / dm.widthPixels;
        if (ratio > 1.8) {
            setContentView(R.layout.activity_scan_q_r_code);
        } else {
            setContentView(R.layout.activity_scan_q_r_code_small);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        db = FirebaseFirestore.getInstance();
        iv = findViewById(R.id.iv);
        previewView = findViewById(R.id.previewView);

        if (checkPermissions()) {
            removeCameraRequiredLayout();
            startCamera();
        } else {
            setCameraRequiredLayout();
        }
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void removeCameraRequiredLayout() {
        Glide.with(this).load("file:///android_asset/scan_qr.gif").into(iv);
    }

    private void setCameraRequiredLayout() {
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                preview.setSurfaceProvider(previewView.createSurfaceProvider());
                analyzer = new ImageAnalysis.Builder().build();
                ImageScanner scanner = new ImageScanner(this);
                analyzer.setAnalyzer(cameraExecutor, scanner);
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        boolean allGranted = true;
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (!allGranted)
            requestPermissions(permissions, RC_CAMERA_PERMISSION);
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_CAMERA_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permission needed for app to work.", Toast.LENGTH_SHORT).show();
            } else {
                removeCameraRequiredLayout();
                startCamera();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onQrResult(String result) {
        if (cameraProvider != null && analyzer != null) {
            cameraProvider.unbind(analyzer);
        }
        final boolean[] valid = {false};
        CollectionReference collection = FirebaseFirestore.getInstance().collection("devices");
        collection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : snapshots) {
                            if (snapshot.get("name").toString().equals(result)) {
                                valid[0] = true;
                                break;
                            }
                        }
                        if (valid[0]) {
                            addDevice(result);
                        } else {
                            Toast.makeText(ScanQRCodeActivity.this, "Device is not registered", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void addDevice(String doc) {
        final int[] userNumber = new int[1];
        final int[] remain = new int[1];
        final boolean[] emailAlreadyAdded = new boolean[1];
        DocumentReference countDb = db.collection("devices").document(doc);
        countDb.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        remain[0] = Integer.parseInt(documentSnapshot.get("remaining").toString());
                        userNumber[0] = Integer.parseInt(documentSnapshot.get("count").toString()) - remain[0];
                        DocumentReference documentReference = db.collection("devices").document(doc).collection("users").document("1");
                        Map<String, Object> deviceDatas = new HashMap<>();
                        documentReference.get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        for (int i = 0; i < userNumber[0]; i++) {
                                            if (documentSnapshot.get(String.valueOf(i)).toString().equals(staticEmail)) {
                                                emailAlreadyAdded[0] = true;
                                                break;
                                            }
                                        }
                                        if (emailAlreadyAdded[0]) {
                                            linkDeviceOnFirebase(doc);
                                        } else {
                                            if (remain[0] > 0) {
                                                deviceDatas.put(String.valueOf(userNumber[0]), staticEmail);
                                                documentReference.set(deviceDatas, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        db.collection("devices").document(doc).update("remaining", (remain[0] - 1));
                                                        linkDeviceOnFirebase(doc);
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(ScanQRCodeActivity.this, "Device scan limit end", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                });
                    }
                });
    }

    private void linkDeviceOnFirebase(String deviceId) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Map<String, Object> map = new HashMap<>();
        map.put(deviceId, System.currentTimeMillis() + (15 * 60 * 1000));
        documentReference.set(map, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ScanQRCodeActivity.this, "Device added successfully", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRCodeActivity.this);
                        builder.setTitle("Please select mode");
                        final View customLayout = getLayoutInflater().inflate(R.layout.mode_select_layout, null);
                        builder.setView(customLayout);
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            RadioGroup radioGroup = customLayout.findViewById(R.id.rgModes);
                            switch (radioGroup.getCheckedRadioButtonId()) {
                                case R.id.rbStandard:
                                    startActivity(new Intent(ScanQRCodeActivity.this, LaunchCameraActivity.class));
                                    break;
                                case R.id.rbDual:
                                    startActivity(new Intent(ScanQRCodeActivity.this, LaunchCameraDualActivity.class));
                                    break;
                            }
                            finish();
//                                Toast.makeText(ScanQRCodeActivity.this,"selected mode is "+(radioGroup.getCheckedRadioButtonId()==R.id.rbStandard?"Standard":"Dual"),Toast.LENGTH_SHORT).show();
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
                        // DONE: 08/12/21 Add dialogue for mode: STANDARD & DUAL . Open activity accordingly (LaunchCameraActivity for standard mode and A new LaunchCameraDual for DUAL)
                        // TODO: 08/12/21 Create the LaunchCameraDual activity and start the CameraActivity from it as startActivityForResult Also pass a key to CameraActivity which will tell it that it has been started for result
                        // TODO: 08/12/21 the  LaunchCameraDualactivity will also contain a proceed button that will lead to graphs activity with all the required data in intent (if both samples have been recorded)

                    }
                });
    }

    private class ImageScanner implements ImageAnalysis.Analyzer {
        OnQrResultListener onQrResultListener;
        BarcodeScanner scanner;

        public ImageScanner(OnQrResultListener onQrResultListener) {
            this.onQrResultListener = onQrResultListener;
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();
            scanner = BarcodeScanning.getClient(options);
        }

        @Override
        public void analyze(@NonNull ImageProxy image) {
            scanImageForQr(image);
        }

        private void scanImageForQr(ImageProxy imageProxy) {
            @SuppressLint({"UnsafeOptInUsageError", "UnsafeExperimentalUsageError"}) Image mediaImage = imageProxy.getImage();
            if (mediaImage == null) {
                return;
            }
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            scanner.process(image).addOnSuccessListener(barcodes -> {
                for (int i = 0; i < barcodes.size(); ++i) {
                    String result = barcodes.get(i).getRawValue();
                    if (validateQrResult(result)) {
                        onQrResultListener.onQrResult(result);
                        break;
                    }
                }
                imageProxy.close();
            });
        }

        private boolean validateQrResult(String rawValue) {
            return true;
        }
    }
}