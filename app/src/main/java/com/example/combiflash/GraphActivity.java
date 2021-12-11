package com.example.combiflash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class GraphActivity extends AppCompatActivity {
    TextView tvTest;
    String mode;
    SampleData[] sampleData = new SampleData[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        tvTest = findViewById(R.id.tvTest);
        Intent intent = getIntent();
        mode = intent.getStringExtra(getResources().getString(R.string.modeKey));
        if (mode.equals(getResources().getString(R.string.modeDual))) {
            sampleData[0] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentration1Key), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArray1Key)));
            sampleData[1] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentration2Key), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArray2Key)));
            tvTest.setText("Mode: " + mode + "\n" + sampleData[0].toString() + sampleData[1].toString());

        } else if (mode.equals(getResources().getString(R.string.modeStandard))) {
            sampleData[0] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentrationKey), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArrayKey)));
            tvTest.setText("Mode: " + mode + "\n" + sampleData[0].toString());
        }

    }
}