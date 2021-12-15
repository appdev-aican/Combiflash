package com.example.combiflash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Arrays;

public class GraphActivity extends AppCompatActivity {
//    TextView tvTest;
    String mode;
    SampleData[] sampleData = new SampleData[2];
    float[][] cvValues=new float[2][];
    private LineChart chart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        chart = findViewById(R.id.chart);
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);

        Intent intent = getIntent();
        mode = intent.getStringExtra(getResources().getString(R.string.modeKey));
        if (mode.equals(getResources().getString(R.string.modeDual))) {
            sampleData[0] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentration1Key), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArray1Key)));
            sampleData[1] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentration2Key), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArray2Key)));
//            tvTest.setText("Mode: " + mode + "\n" + sampleData[0].toString() + sampleData[1].toString());
            cvValues[0]=sampleData[0].getSortedCvValues();
            cvValues[1]=sampleData[1].getSortedCvValues();

        } else if (mode.equals(getResources().getString(R.string.modeStandard))) {
            sampleData[0] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentrationKey), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArrayKey)));
            cvValues[0]=sampleData[0].getSortedCvValues();
            Log.e("TAG", "onCreate: "+ Arrays.toString(sampleData[0].getRfValues()));
            Log.e("TAG", "onCreate: "+ Arrays.toString(cvValues[0]));
            drawGraphStandard();

            //            tvTest.setText("Mode: " + mode + "\n" + sampleData[0].toString());
        }


    }

    private void drawGraphStandard() {
        Legend l = chart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
//        xAxis.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//            }
//        });

        ArrayList<ILineDataSet> data=new ArrayList<>();
        data.add(generateLineDataStandard());
        data.addAll(generateVerticalLineDataStandard());
//        xAxis.setAxisMaximum(Math.max(data.get(0).getXMax(),data.get(1).getXMax()) + 0.25f);
        xAxis.setAxisMaximum(cvValues[0][cvValues[0].length-1]+cvValues[0][0]);
        LineData lineData=new LineData(data);
        chart.setTouchEnabled(true);
        chart.setData(lineData);
        chart.invalidate();
    }

    @NonNull
    private LineDataSet generateLineDataStandard() {

//        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<>();
        Log.e("TAG", "generateLineDataStandard: "+ Arrays.toString(cvValues[0]));

        entries.add(new Entry(cvValues[0][0],sampleData[0].concentration/4));
        entries.add(new Entry(cvValues[0][0]+(cvValues[0][cvValues[0].length-1]-cvValues[0][0])/13,sampleData[0].concentration/4));
        entries.add(new Entry(cvValues[0][0]+10*(cvValues[0][cvValues[0].length-1]-cvValues[0][0])/13,sampleData[0].concentration*2));
        entries.add(new Entry(cvValues[0][cvValues[0].length-1],sampleData[0].concentration*2));
        LineDataSet set = new LineDataSet(entries, "Line DataSet");
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