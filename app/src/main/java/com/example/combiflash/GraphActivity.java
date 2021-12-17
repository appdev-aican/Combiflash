package com.example.combiflash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;

public class GraphActivity extends AppCompatActivity {
    String mode;
    SampleData[] sampleData = new SampleData[2];
    float[][] cvValues=new float[2][];
    private LineChart chart;
    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        spinner=findViewById(R.id.spGraph);
        chart = findViewById(R.id.chart);
        getDataFromIntent();
        setupChart();
        setUpSpinner();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        mode = intent.getStringExtra(getResources().getString(R.string.modeKey));
        if (mode.equals(getResources().getString(R.string.modeDual))) {
            sampleData[0] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentration1Key), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArray1Key)));
            sampleData[1] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentration2Key), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArray2Key)));
            cvValues[0]=sampleData[0].getSortedCvValues();
            cvValues[1]=sampleData[1].getSortedCvValues();

        } else if (mode.equals(getResources().getString(R.string.modeStandard))) {
            sampleData[0] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentrationKey), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArrayKey)));
            cvValues[0]=sampleData[0].getSortedCvValues();
        }
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        Legend l = chart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
    }

    private void setUpSpinner() {
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.graphTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("TAG", "onItemSelected: "+position+" "+id );
                ArrayList<ILineDataSet> data=new ArrayList<>();
                if (mode.equals(getResources().getString(R.string.modeDual))) {
                    switch (position){
                        case 0:
                            data.add(generateLineDataDualEqn1());
                            break;
                        case 1:
                            data.add(generateLineDataDualEqn2());
                            break;
                        case 2:
                            data.add(generateLineDataDualEqn3());
                            break;
                    }
                } else if (mode.equals(getResources().getString(R.string.modeStandard))) {
                    switch (position){
                        case 0:
                            data.add(generateLineDataStandardEqn1());
                            break;
                        case 1:
                            data.add(generateLineDataStandardEqn2());
                            break;
                        case 2:
                            data.add(generateLineDataStandardEqn3());
                            break;
                    }
                    data.addAll(generateVerticalLineDataStandard());
                    chart.getXAxis().setAxisMaximum(cvValues[0][cvValues[0].length-1]+cvValues[0][0]);
                }
                LineData lineData=new LineData(data);
                chart.setData(lineData);
                chart.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private ILineDataSet generateLineDataDualEqn3() {
        ArrayList<Entry> entries = new ArrayList<>();
        LineDataSet set = new LineDataSet(entries, "Line DataSet 2");
        return set;
    }

    private ILineDataSet generateLineDataDualEqn2() {
        ArrayList<Entry> entries = new ArrayList<>();
        LineDataSet set = new LineDataSet(entries, "Line DataSet 2");
        return set;
    }

    private ILineDataSet generateLineDataDualEqn1() {
        ArrayList<Entry> entries = new ArrayList<>();
        LineDataSet set = new LineDataSet(entries, "Line DataSet 2");
        return set;
    }

    @NonNull
    private LineDataSet generateLineDataStandardEqn3() {
        ArrayList<Entry> entries = new ArrayList<>();
        Log.e("TAG", "generateLineDataStandard: "+ Arrays.toString(cvValues[0]));
        entries.add(new Entry(cvValues[0][0],sampleData[0].concentration/4));
        entries.add(new Entry(cvValues[0][0]+(cvValues[0][cvValues[0].length-1]-cvValues[0][0])/13,sampleData[0].concentration/4));
        entries.add(new Entry(cvValues[0][0]+11*(cvValues[0][cvValues[0].length-1]-cvValues[0][0])/13,Math.min(100,sampleData[0].concentration*2)));
        entries.add(new Entry(cvValues[0][cvValues[0].length-1],Math.min(100,sampleData[0].concentration*2)));
        LineDataSet set = new LineDataSet(entries, "Line DataSet 3");
        set.setColor(Color.rgb(31, 136, 222));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }
    private LineDataSet generateLineDataStandardEqn2(){
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(cvValues[0][0],sampleData[0].concentration/4));
        entries.add(new Entry(cvValues[0][0]+(cvValues[0][cvValues[0].length-1]-cvValues[0][0])/13,sampleData[0].concentration/4));
        entries.add(new Entry(cvValues[0][0]+11*(cvValues[0][cvValues[0].length-1]-cvValues[0][0])/13,Math.min(100,sampleData[0].concentration)));
        entries.add(new Entry(cvValues[0][cvValues[0].length-1],Math.min(100,sampleData[0].concentration)));
        LineDataSet set = new LineDataSet(entries, "Line DataSet 2");
        set.setColor(Color.rgb(31, 136, 222));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }
    private LineDataSet generateLineDataStandardEqn1(){
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0,0));
        entries.add(new Entry(cvValues[0][cvValues[0].length-1]*1.25f,sampleData[0].getConcentration()));
        LineDataSet set = new LineDataSet(entries, "Line DataSet 1");
        set.setColor(Color.rgb(31, 136, 222));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }
    @NonNull
    private ArrayList<LineDataSet> generateVerticalLineDataStandard() {

        ArrayList<LineDataSet> sets=new ArrayList<>();
        for(int i=0;i<cvValues[0].length;i++)
        {
            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(cvValues[0][i],0f));
            entries.add(new Entry(cvValues[0][i],sampleData[0].getConcentration()*2+17f));
            LineDataSet set = new LineDataSet(entries, "Line DataSet cv");
            set.setColor(Color.rgb(199, 78, 54));
            set.setLineWidth(1f);
            set.setDrawCircles(false);
            set.setMode(LineDataSet.Mode.LINEAR);
            set.setDrawValues(false);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            sets.add(set);
        }
        return sets;
    }

}