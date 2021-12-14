package com.example.combiflash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.CombinedChart;
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

import java.util.ArrayList;
import java.util.Arrays;

public class GraphActivity extends AppCompatActivity {
//    TextView tvTest;
    String mode;
    SampleData[] sampleData = new SampleData[2];
    float[][] cvValues=new float[2][];
    private CombinedChart chart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        chart = findViewById(R.id.chart);
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.BUBBLE, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });
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
        CombinedData data = new CombinedData();

        data.setData(generateLineDataStandard());
        data.setData(generateBarDataStandard());
//        data.setValueTypeface(tfLight);
        xAxis.setAxisMaximum(data.getXMax() + 0.25f);
        chart.setTouchEnabled(true);
        chart.setData(data);
        chart.invalidate();
    }

    private BarData generateBarDataStandard() {
        ArrayList<BarEntry> entries1 = new ArrayList<>();
        ArrayList<BarEntry> entries2 = new ArrayList<>();

        for(int i=0;i<cvValues[0].length;i++)
        {
            entries1.add(new BarEntry(cvValues[0][i],sampleData[0].getConcentration()*2+10f));
        }
//        for (int index = 0; index < count; index++) {
//            entries1.add(new BarEntry(0, getRandom(25, 25)));
//
//            // stacked
//            entries2.add(new BarEntry(0, new float[]{getRandom(13, 12), getRandom(13, 12)}));
//        }

        BarDataSet set1 = new BarDataSet(entries1, "Bar 1");
        set1.setColor(Color.rgb(199, 78, 54));
//        set1.setValueTextColor(Color.rgb(60, 220, 78));
        set1.setDrawValues(false);
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);


        float groupSpace = 0.06f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.01f; // x2 dataset
        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"

        BarData d = new BarData(set1);
        d.setBarWidth(barWidth);

        // make this BarData object grouped
//        d.groupBars(0, groupSpace, barSpace); // start at x = 0

        return d;
    }

    private LineData generateLineDataStandard() {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<>();
        Log.e("TAG", "generateLineDataStandard: "+ Arrays.toString(cvValues[0]));

        entries.add(new Entry(cvValues[0][0],sampleData[0].concentration/4));
        entries.add(new Entry(cvValues[0][0]+(cvValues[0][cvValues[0].length-1]-cvValues[0][0])/13,sampleData[0].concentration/4));
        entries.add(new Entry(cvValues[0][0]+10*(cvValues[0][cvValues[0].length-1]-cvValues[0][0])/13,sampleData[0].concentration*2));
        entries.add(new Entry(cvValues[0][cvValues[0].length-1],sampleData[0].concentration*2));
        LineDataSet set = new LineDataSet(entries, "Line DataSet");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(1f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(240, 238, 70));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        d.addDataSet(set);

        return d;
    }


}