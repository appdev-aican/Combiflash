package com.example.combiflash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

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

public class GraphActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    String mode;
    SampleData[] sampleData = new SampleData[2];
    float[][] cvValues = new float[2][];
    private LineChart chart;
    Spinner spinner;

    SeekBar sbPercent, sbHold;
    TextView tvPercentX, tvHold;
    int percentageX = 60, hold = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        spinner = findViewById(R.id.spGraph);
        chart = findViewById(R.id.chart);
        sbHold = findViewById(R.id.sbHold);
        sbPercent = findViewById(R.id.sbPercentageX);
        sbHold.setOnSeekBarChangeListener(this);
        sbPercent.setOnSeekBarChangeListener(this);
        tvPercentX = findViewById(R.id.tvPercentage);
        tvHold = findViewById(R.id.tvHold);
        sbPercent.setVisibility(View.GONE);
        sbHold.setVisibility(View.GONE);
        tvHold.setVisibility(View.GONE);
        tvPercentX.setVisibility(View.GONE);
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
            cvValues[0] = sampleData[0].getSortedCvValues();
            cvValues[1] = sampleData[1].getSortedCvValues();

        } else if (mode.equals(getResources().getString(R.string.modeStandard))) {
            sampleData[0] = new SampleData(intent.getFloatExtra(getResources().getString(R.string.concentrationKey), -1.0f), intent.getFloatArrayExtra(getResources().getString(R.string.rfArrayKey)));
            cvValues[0] = sampleData[0].getSortedCvValues();
        }
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
//        chart.getXAxis().setLabelCount(5, true);
        Legend l = chart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(120f);
        rightAxis.setLabelCount(12);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(120f);
        leftAxis.setLabelCount(12);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(0.0001f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
    }

    private void setUpSpinner() {
        ArrayAdapter<CharSequence> adapter;
        if (mode.equals(getResources().getString(R.string.modeDual))) {
            adapter = ArrayAdapter.createFromResource(this, R.array.graphTypesDual, android.R.layout.simple_spinner_item);
        } else {
            adapter = ArrayAdapter.createFromResource(this, R.array.graphTypesStandard, android.R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("TAG", "onItemSelected: " + position + " " + id);
                ArrayList<ILineDataSet> data = new ArrayList<>();
                if (mode.equals(getResources().getString(R.string.modeDual))) {
                    switch (position) {
                        case 0:
                            sbPercent.setVisibility(View.GONE);
                            sbHold.setVisibility(View.GONE);
                            tvHold.setVisibility(View.GONE);
                            tvPercentX.setVisibility(View.GONE);
                            data.add(generateLineDataDualEqn1());
                            chart.getXAxis().setAxisMaximum(data.get(0).getXMax());
                            break;
                        case 1:
                            sbPercent.setVisibility(View.GONE);
                            sbHold.setVisibility(View.GONE);
                            tvHold.setVisibility(View.GONE);
                            tvPercentX.setVisibility(View.GONE);
                            data.add(generateLineDataDualEqn2());
                            chart.getXAxis().setAxisMaximum(data.get(0).getXMax());
                            break;
                        case 2:
                            sbPercent.setVisibility(View.GONE);
                            sbHold.setVisibility(View.GONE);
                            tvHold.setVisibility(View.GONE);
                            tvPercentX.setVisibility(View.GONE);
                            data.add(generateLineDataDualEqn3());
                            chart.getXAxis().setAxisMaximum(data.get(0).getXMax());
                            break;
                    }
                } else if (mode.equals(getResources().getString(R.string.modeStandard))) {
                    switch (position) {
                        case 0:
                            sbPercent.setVisibility(View.GONE);
                            sbHold.setVisibility(View.GONE);
                            tvHold.setVisibility(View.GONE);
                            tvPercentX.setVisibility(View.GONE);
                            data.add(generateLineDataStandardEqn1());
                            chart.getXAxis().setAxisMaximum(cvValues[0][cvValues[0].length - 1] * 1.25f);
                            break;
                        case 1:
                            sbPercent.setVisibility(View.GONE);
                            sbHold.setVisibility(View.GONE);
                            tvHold.setVisibility(View.GONE);
                            tvPercentX.setVisibility(View.GONE);
                            data.add(generateLineDataStandardEqn2());
                            chart.getXAxis().setAxisMaximum(cvValues[0][cvValues[0].length - 1] + cvValues[0][0]);
                            break;
                        case 2:
                            sbPercent.setVisibility(View.GONE);
                            sbHold.setVisibility(View.GONE);
                            tvHold.setVisibility(View.GONE);
                            tvPercentX.setVisibility(View.GONE);
                            data.add(generateLineDataStandardEqn3());
                            chart.getXAxis().setAxisMaximum(cvValues[0][cvValues[0].length - 1] + cvValues[0][0]);
                            break;
                        case 3:
                            sbPercent.setVisibility(View.VISIBLE);
                            sbHold.setVisibility(View.VISIBLE);
                            tvHold.setVisibility(View.VISIBLE);
                            tvPercentX.setVisibility(View.VISIBLE);
                            data.add(generateLineDataStandardEqn4());
                            chart.getXAxis().setAxisMaximum(data.get(0).getXMax());
                            break;

                    }
                    data.addAll(generateVerticalLineDataStandard());
                }
                LineData lineData = new LineData(data);
                chart.setData(lineData);
                chart.fitScreen();
                chart.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @NonNull
    private ILineDataSet generateLineDataDualEqn3() {
        float cf, bf;
        float cf1, cf2, bf1, bf2;
        float cv1, cv2, range, b1, b2;

        Log.e("TAG", "Data 1 -----------------------");
        Log.e("TAG", "concentration=" + sampleData[0].getConcentration());
        Log.e("TAG", "rf values=" + Arrays.toString(sampleData[0].getRfValues()));
        Log.e("TAG", "cv values=" + Arrays.toString(cvValues[0]) + "\n");
        //from sample 1
        cv1 = cvValues[0][0];
        cv2 = cvValues[0][1];
        range = cvValues[0][cvValues[0].length - 1];
        for (int i = 1; i < cvValues[0].length; i++) {
            if (cvValues[0][i] - cvValues[0][i - 1] < cv2 - cv1) {
                cv1 = cvValues[0][i - 1];
                cv2 = cvValues[0][i];
            }
        }
        Log.e("TAG", "Close cv values obtained= " + cv1 + " and " + cv2);

        cf1 = (cv1 + cv2) / 2;
        Log.e("TAG", "Thus Ci = " + cf1);
        Log.e("TAG", "c values range is  = " + range);
        Log.e("TAG", "first part till  = " + (range / 13));
        Log.e("TAG", "second part till  = " + (11 * range / 13));
        Log.e("TAG", "third part till  = end cv");

        if (cv1 < range / 13f) {
            Log.e("TAG", "first cv value lies in first part thus b1 set to conc/4 " + sampleData[0].getConcentration() / 4);

            b1 = sampleData[0].getConcentration() / 4;
        } else if (cv1 < 11 * range / 13f) {
            Log.e("TAG", "first cv value lies in second part thus calculating b1 with line eqn");
            Log.e("TAG", "x1=" + (range / 13));
            Log.e("TAG", "x2=" + (11 * range / 13));
            Log.e("TAG", "y1=" + sampleData[0].getConcentration() / 4);
            Log.e("TAG", "y2=" + Math.min(100f, 2 * sampleData[0].getConcentration()));

            float x1 = range / 13;
            float x2 = 11 * range / 13;
            float y1 = sampleData[0].getConcentration() / 4;
            float y2 = Math.min(100f, 2 * sampleData[0].getConcentration());
            float v = (y2 - y1) / (x2 - x1) * (cv1 - x1);
            b1 = y1 + v;
            Log.e("TAG", "b1 set to = " + (y1 + v));

        } else {
            b1 = Math.min(100f, 2 * sampleData[0].getConcentration());
            Log.e("TAG", "first cv value lies in third part thus b1 set to conc*2(cap 100) " + Math.min(100f, 2 * sampleData[0].getConcentration()));

        }
        if (cv2 < range / 13f) {
            Log.e("TAG", "second cv value lies in first part thus b2 set to conc/4 " + sampleData[0].getConcentration() / 4);
            b2 = sampleData[0].getConcentration() / 4;
        } else if (cv2 < 11 * range / 13f) {
            Log.e("TAG", "second cv value lies in second part thus calculating b1 with line eqn");
            Log.e("TAG", "x1=" + (range / 13));
            Log.e("TAG", "x2=" + (11 * range / 13));
            Log.e("TAG", "y1=" + (sampleData[0].getConcentration() / 4));
            Log.e("TAG", "y2=" + Math.min(100f, 2 * sampleData[0].getConcentration()));

            float x1 = range / 13;
            float x2 = 11 * range / 13;
            float y1 = sampleData[0].getConcentration() / 4;
            float y2 = Math.min(100f, 2 * sampleData[0].getConcentration());
            float v = (y2 - y1) / (x2 - x1) * (cv2 - x1);
            Log.e("TAG", "b2 set to = " + (y1 + v));
            b2 = y1 + v;
        } else {
            b2 = Math.min(100f, 2 * sampleData[0].getConcentration());
            Log.e("TAG", "second cv value lies in third part thus b2 set to conc*2(cap 100) " + Math.min(100f, 2 * sampleData[0].getConcentration()));
        }
        bf1 = (b1 + b2) / 2;
        Log.e("TAG", "Thus Bi = " + bf1);

        Log.e("TAG", "\n\nData 2 -----------------------");
        Log.e("TAG", "concentration=" + sampleData[1].getConcentration());
        Log.e("TAG", "rf values=" + Arrays.toString(sampleData[1].getRfValues()));
        Log.e("TAG", "cv values=" + Arrays.toString(cvValues[1]) + "\n");
        //from sample 2
        cv1 = cvValues[1][0];
        cv2 = cvValues[1][1];
        range = cvValues[1][cvValues[1].length - 1];
        for (int i = 1; i < cvValues[1].length; i++) {
            if (cvValues[1][i] - cvValues[1][i - 1] < cv2 - cv1) {
                cv1 = cvValues[1][i - 1];
                cv2 = cvValues[1][i];
            }
        }
        Log.e("TAG", "Close cv values obtained= " + cv1 + " and " + cv2);
        cf2 = (cv1 + cv2) / 2;
        Log.e("TAG", "Thus Cii = " + cf2);
        Log.e("TAG", "c values range is  = " + range);
        Log.e("TAG", "first part till  = " + (range / 13));
        Log.e("TAG", "second part till  = " + (11 * range / 13));
        Log.e("TAG", "third part till  = end cv");

        if (cv1 < range / 13f) {
            Log.e("TAG", "first cv value lies in first part thus b1 set to conc/4 " + sampleData[1].getConcentration() / 4);
            b1 = sampleData[1].getConcentration() / 4;
        } else if (cv1 < 11 * range / 13f) {
            Log.e("TAG", "first cv value lies in second part thus calculating b1 with line eqn");
            Log.e("TAG", "x1=" + (range / 13));
            Log.e("TAG", "x2=" + (11 * range / 13));
            Log.e("TAG", "y1=" + sampleData[1].getConcentration() / 4);
            Log.e("TAG", "y2=" + Math.min(100f, 2 * sampleData[1].getConcentration()));
            float x1 = range / 13;
            float x2 = 11 * range / 13;
            float y1 = sampleData[1].getConcentration() / 4;
            float y2 = Math.min(100f, 2 * sampleData[1].getConcentration());
            final float v = (y2 - y1) / (x2 - x1) * (cv1 - x1);
            b1 = y1 + v;
            Log.e("TAG", "b1 set to = " + y1 + v);
        } else {
            b1 = Math.min(100f, 2 * sampleData[1].getConcentration());
            Log.e("TAG", "first cv value lies in third part thus b1 set to conc*2(cap 100) " + Math.min(100f, 2 * sampleData[1].getConcentration()));
        }
        if (cv2 < range / 13f) {
            Log.e("TAG", "second cv value lies in first part thus b2 set to conc/4 " + sampleData[1].getConcentration() / 4);
            b2 = sampleData[1].getConcentration() / 4;
        } else if (cv2 < 11 * range / 13f) {
            Log.e("TAG", "second cv value lies in second part thus calculating b1 with line eqn");
            Log.e("TAG", "x1=" + (range / 13));
            Log.e("TAG", "x2=" + (11 * range / 13));
            Log.e("TAG", "y1=" + sampleData[1].getConcentration() / 4);
            Log.e("TAG", "y2=" + Math.min(100f, 2 * sampleData[1].getConcentration()));

            float x1 = range / 13;
            float x2 = 11 * range / 13;
            float y1 = sampleData[1].getConcentration() / 4;
            float y2 = Math.min(100f, 2 * sampleData[1].getConcentration());
            float v = (y2 - y1) / (x2 - x1) * (cv2 - x1);
            Log.e("TAG", "b2 set to = " + (y1 + v));
            b2 = y1 + v;
        } else {
            b2 = Math.min(100f, 2 * sampleData[1].getConcentration());
            Log.e("TAG", "second cv value lies in third part thus b2 set to conc*2(cap 100) " + Math.min(100f, 2 * sampleData[1].getConcentration()));
        }
        bf2 = (b1 + b2) / 2;
        Log.e("TAG", "Thus Bii = " + bf2);

        bf = (bf1 + bf2) / 2;
        cf = cf1 + cf2 - 4;
        Log.e("TAG", "\n\nThus cf = " + cf);
        Log.e("TAG", "Thus bf = " + bf);

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, bf / 4));
        entries.add(new Entry(1, bf / 4));
        entries.add(new Entry(cf, bf));
        entries.add(new Entry(cf + 6, bf));
        entries.add(new Entry((4.0f * (cf - 1) * (Math.min(100f, 2 * bf) - bf) + 3 * bf * cf + 18 * bf) / (3.0f * bf), Math.min(100f, 2 * bf)));
        entries.add(new Entry((4.0f * (cf - 1) * (Math.min(100f, 2 * bf) - bf) + 3 * bf * cf + 18 * bf) / (3.0f * bf), Math.min(100f, 2 * bf)));
        Log.e("TAG", "points = " + entries);

        LineDataSet set = new LineDataSet(entries, "Line DataSet 3");
        set.setColor(Color.rgb(31, 136, 222));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    @NonNull
    private ILineDataSet generateLineDataDualEqn2() {
        float cf, bf;
        float cf1, cf2, bf1, bf2;
        float cv1, cv2, range, b1, b2;

        Log.e("TAG", "Data 1 -----------------------");
        Log.e("TAG", "concentration=" + sampleData[0].getConcentration());
        Log.e("TAG", "rf values=" + Arrays.toString(sampleData[0].getRfValues()));
        Log.e("TAG", "cv values=" + Arrays.toString(cvValues[0]) + "\n");
        //from sample 1
        cv1 = cvValues[0][0];
        cv2 = cvValues[0][1];
        range = cvValues[0][cvValues[0].length - 1];
        for (int i = 1; i < cvValues[0].length; i++) {
            if (cvValues[0][i] - cvValues[0][i - 1] < cv2 - cv1) {
                cv1 = cvValues[0][i - 1];
                cv2 = cvValues[0][i];
            }
        }
        Log.e("TAG", "Close cv values obtained= " + cv1 + " and " + cv2);

        cf1 = (cv1 + cv2) / 2;
        Log.e("TAG", "Thus Ci = " + cf1);
        Log.e("TAG", "c values range is  = " + range);
        Log.e("TAG", "first part till  = " + (range / 13));
        Log.e("TAG", "second part till  = " + (11 * range / 13));
        Log.e("TAG", "third part till  = end cv");

        if (cv1 < range / 13f) {
            Log.e("TAG", "first cv value lies in first part thus b1 set to conc/4 " + sampleData[0].getConcentration() / 4);

            b1 = sampleData[0].getConcentration() / 4;
        } else if (cv1 < 11 * range / 13f) {
            Log.e("TAG", "first cv value lies in second part thus calculating b1 with line eqn");
            Log.e("TAG", "x1=" + (range / 13));
            Log.e("TAG", "x2=" + (11 * range / 13));
            Log.e("TAG", "y1=" + sampleData[0].getConcentration() / 4);
            Log.e("TAG", "y2=" + Math.min(100f, sampleData[0].getConcentration()));

            float x1 = range / 13;
            float x2 = 11 * range / 13;
            float y1 = sampleData[0].getConcentration() / 4;
            float y2 = Math.min(100f, sampleData[0].getConcentration());
            float v = (y2 - y1) / (x2 - x1) * (cv1 - x1);
            b1 = y1 + v;
            Log.e("TAG", "b1 set to = " + (y1 + v));

        } else {
            b1 = Math.min(100f, sampleData[0].getConcentration());
            Log.e("TAG", "first cv value lies in third part thus b1 set to conc(cap 100) " + Math.min(100f, sampleData[0].getConcentration()));

        }
        if (cv2 < range / 13f) {
            Log.e("TAG", "second cv value lies in first part thus b2 set to conc/4 " + sampleData[0].getConcentration() / 4);
            b2 = sampleData[0].getConcentration() / 4;
        } else if (cv2 < 11 * range / 13f) {
            Log.e("TAG", "second cv value lies in second part thus calculating b1 with line eqn");
            Log.e("TAG", "x1=" + (range / 13));
            Log.e("TAG", "x2=" + (11 * range / 13));
            Log.e("TAG", "y1=" + (sampleData[0].getConcentration() / 4));
            Log.e("TAG", "y2=" + Math.min(100f, sampleData[0].getConcentration()));

            float x1 = range / 13;
            float x2 = 11 * range / 13;
            float y1 = sampleData[0].getConcentration() / 4;
            float y2 = Math.min(100f, sampleData[0].getConcentration());
            float v = (y2 - y1) / (x2 - x1) * (cv2 - x1);
            Log.e("TAG", "b2 set to = " + (y1 + v));
            b2 = y1 + v;
        } else {
            b2 = Math.min(100f, sampleData[0].getConcentration());
            Log.e("TAG", "second cv value lies in third part thus b2 set to conc*2(cap 100) " + Math.min(100f, sampleData[0].getConcentration()));
        }
        bf1 = (b1 + b2) / 2;
        Log.e("TAG", "Thus Bi = " + bf1);

        Log.e("TAG", "\n\nData 2 -----------------------");
        Log.e("TAG", "concentration=" + sampleData[1].getConcentration());
        Log.e("TAG", "rf values=" + Arrays.toString(sampleData[1].getRfValues()));
        Log.e("TAG", "cv values=" + Arrays.toString(cvValues[1]) + "\n");
        //from sample 2
        cv1 = cvValues[1][0];
        cv2 = cvValues[1][1];
        range = cvValues[1][cvValues[1].length - 1];
        for (int i = 1; i < cvValues[1].length; i++) {
            if (cvValues[1][i] - cvValues[1][i - 1] < cv2 - cv1) {
                cv1 = cvValues[1][i - 1];
                cv2 = cvValues[1][i];
            }
        }
        Log.e("TAG", "Close cv values obtained= " + cv1 + " and " + cv2);
        cf2 = (cv1 + cv2) / 2;
        Log.e("TAG", "Thus Cii = " + cf2);
        Log.e("TAG", "c values range is  = " + range);
        Log.e("TAG", "first part till  = " + (range / 13));
        Log.e("TAG", "second part till  = " + (11 * range / 13));
        Log.e("TAG", "third part till  = end cv");

        if (cv1 < range / 13f) {
            Log.e("TAG", "first cv value lies in first part thus b1 set to conc/4 " + sampleData[1].getConcentration() / 4);
            b1 = sampleData[1].getConcentration() / 4;
        } else if (cv1 < 11 * range / 13f) {
            Log.e("TAG", "first cv value lies in second part thus calculating b1 with line eqn");
            Log.e("TAG", "x1=" + (range / 13));
            Log.e("TAG", "x2=" + (11 * range / 13));
            Log.e("TAG", "y1=" + sampleData[1].getConcentration() / 4);
            Log.e("TAG", "y2=" + Math.min(100f, sampleData[1].getConcentration()));
            float x1 = range / 13;
            float x2 = 11 * range / 13;
            float y1 = sampleData[1].getConcentration() / 4;
            float y2 = Math.min(100f, sampleData[1].getConcentration());
            final float v = (y2 - y1) / (x2 - x1) * (cv1 - x1);
            b1 = y1 + v;
            Log.e("TAG", "b1 set to = " + y1 + v);
        } else {
            b1 = Math.min(100f, sampleData[1].getConcentration());
            Log.e("TAG", "first cv value lies in third part thus b1 set to conc*2(cap 100) " + Math.min(100f, sampleData[1].getConcentration()));
        }
        if (cv2 < range / 13f) {
            Log.e("TAG", "second cv value lies in first part thus b2 set to conc/4 " + sampleData[1].getConcentration() / 4);
            b2 = sampleData[1].getConcentration() / 4;
        } else if (cv2 < 11 * range / 13f) {
            Log.e("TAG", "second cv value lies in second part thus calculating b1 with line eqn");
            Log.e("TAG", "x1=" + (range / 13));
            Log.e("TAG", "x2=" + (11 * range / 13));
            Log.e("TAG", "y1=" + sampleData[1].getConcentration() / 4);
            Log.e("TAG", "y2=" + Math.min(100f, sampleData[1].getConcentration()));

            float x1 = range / 13;
            float x2 = 11 * range / 13;
            float y1 = sampleData[1].getConcentration() / 4;
            float y2 = Math.min(100f, sampleData[1].getConcentration());
            float v = (y2 - y1) / (x2 - x1) * (cv2 - x1);
            Log.e("TAG", "b2 set to = " + (y1 + v));
            b2 = y1 + v;
        } else {
            b2 = Math.min(100f, sampleData[1].getConcentration());
            Log.e("TAG", "second cv value lies in third part thus b2 set to conc*2(cap 100) " + Math.min(100f, sampleData[1].getConcentration()));
        }
        bf2 = (b1 + b2) / 2;
        Log.e("TAG", "Thus Bii = " + bf2);

        bf = (bf1 + bf2) / 2;
        cf = cf1 + cf2 - 4;
        Log.e("TAG", "\n\nThus cf = " + cf);
        Log.e("TAG", "Thus bf = " + bf);

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, bf / 4));
        entries.add(new Entry(1, bf / 4));
        entries.add(new Entry(cf, bf));
        entries.add(new Entry(cf + 6, bf));
        entries.add(new Entry(Math.max(cvValues[0][cvValues[0].length - 1], cvValues[1][cvValues[1].length - 1]), Math.min(100f, Math.max(sampleData[0].getConcentration(), sampleData[1].getConcentration()))));
        entries.add(new Entry(Math.max(cvValues[0][cvValues[0].length - 1], cvValues[1][cvValues[1].length - 1]), Math.min(100f, Math.max(sampleData[0].getConcentration(), sampleData[1].getConcentration()))));
        Log.e("TAG", "points = " + entries);

        LineDataSet set = new LineDataSet(entries, "Line DataSet 2");
        set.setColor(Color.rgb(31, 136, 222));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    @NonNull
    private ILineDataSet generateLineDataDualEqn1() {
        float cf, bf;
        float cf1, cf2, bf1, bf2;
        float cv1, cv2, range, b1, b2;

        Log.e("TAG", "Data 1 -----------------------");
        Log.e("TAG", "concentration=" + sampleData[0].getConcentration());
        Log.e("TAG", "rf values=" + Arrays.toString(sampleData[0].getRfValues()));
        Log.e("TAG", "cv values=" + Arrays.toString(cvValues[0]) + "\n");
        //from sample 1
        cv1 = cvValues[0][0];
        cv2 = cvValues[0][1];
        range = cvValues[0][cvValues[0].length - 1] * 1.25f;
        for (int i = 1; i < cvValues[0].length; i++) {
            if (cvValues[0][i] - cvValues[0][i - 1] < cv2 - cv1) {
                cv1 = cvValues[0][i - 1];
                cv2 = cvValues[0][i];
            }
        }
        Log.e("TAG", "Close cv values obtained= " + cv1 + " and " + cv2);

        cf1 = (cv1 + cv2) / 2;
        Log.e("TAG", "Thus Ci = " + cf1);
        Log.e("TAG", "range is  = " + range);

        Log.e("TAG", "b1 set to cv1*conc/range " + sampleData[0].getConcentration() * cv1 / range);
        b1 = sampleData[0].getConcentration() * cv1 / range;
        Log.e("TAG", "b2 set to cv2*conc/range " + sampleData[0].getConcentration() * cv2 / range);
        b2 = sampleData[0].getConcentration() * cv2 / range;
        bf1 = (b1 + b2) / 2;
        Log.e("TAG", "Thus Bi = " + bf1);

        Log.e("TAG", "\n\nData 2 -----------------------");
        Log.e("TAG", "concentration=" + sampleData[1].getConcentration());
        Log.e("TAG", "rf values=" + Arrays.toString(sampleData[1].getRfValues()));
        Log.e("TAG", "cv values=" + Arrays.toString(cvValues[1]) + "\n");
        //from sample 2
        cv1 = cvValues[1][0];
        cv2 = cvValues[1][1];
        range = cvValues[1][cvValues[1].length - 1] * 1.25f;
        for (int i = 1; i < cvValues[1].length; i++) {
            if (cvValues[1][i] - cvValues[1][i - 1] < cv2 - cv1) {
                cv1 = cvValues[1][i - 1];
                cv2 = cvValues[1][i];
            }
        }
        Log.e("TAG", "Close cv values obtained= " + cv1 + " and " + cv2);
        cf2 = (cv1 + cv2) / 2;
        Log.e("TAG", "Thus Cii = " + cf2);
        Log.e("TAG", "range is  = " + range);

        Log.e("TAG", "b1 set to cv1*conc/range " + sampleData[1].getConcentration() * cv1 / range);
        b1 = sampleData[1].getConcentration() * cv1 / range;
        Log.e("TAG", "b2 set to cv2*conc/range " + sampleData[1].getConcentration() * cv2 / range);
        b2 = sampleData[1].getConcentration() * cv2 / range;
        bf2 = (b1 + b2) / 2;
        Log.e("TAG", "Thus Bii = " + bf2);

        bf = (bf1 + bf2) / 2;
        cf = cf1 + cf2 - 4;
        Log.e("TAG", "\n\nThus cf = " + cf);
        Log.e("TAG", "Thus bf = " + bf);

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0));
        entries.add(new Entry(cf, bf));
        entries.add(new Entry(cf + 6, bf));
        entries.add(new Entry(Math.max(cvValues[0][cvValues[0].length - 1], cvValues[1][cvValues[1].length - 1]), bf * (Math.max(cvValues[0][cvValues[0].length - 1], cvValues[1][cvValues[1].length - 1]) - 6) / cf));
        entries.add(new Entry(Math.max(cvValues[0][cvValues[0].length - 1], cvValues[1][cvValues[1].length - 1]), bf * (Math.max(cvValues[0][cvValues[0].length - 1], cvValues[1][cvValues[1].length - 1]) - 6) / cf));
        Log.e("TAG", "points = " + entries);
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
    private ILineDataSet generateLineDataStandardEqn4() {
        float cv1, cv2;
        cv1 = cvValues[0][0];
        cv2 = cvValues[0][1];
        for (int i = 1; i < cvValues[0].length; i++) {
            if (cvValues[0][i] - cvValues[0][i - 1] < cv2 - cv1) {
                cv1 = cvValues[0][i - 1];
                cv2 = cvValues[0][i];
            }
        }
        Log.e("TAG", "Close cv values obtained= " + cv1 + " and " + cv2);

        float x1 = sampleData[0].getConcentration() * (1 - 1 / cv1);
        float cvMax = cvValues[0][cvValues[0].length - 1];
        float b = sampleData[0].getConcentration();
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0));
        entries.add(new Entry(percentageX/100f * x1 * cvMax * 1.25f / b, percentageX/100f * x1));
        entries.add(new Entry((percentageX/100f * x1 * cvMax * 1.25f / b) + hold, percentageX/100f * x1));
        entries.add(new Entry(hold + 1.25f * cvMax, b));
        entries.add(new Entry(hold + 1.25f * cvMax, b));
        Log.e("TAG", "Entries: " + entries);
        LineDataSet set = new LineDataSet(entries, "Line DataSet 4");
        set.setColor(Color.rgb(31, 136, 222));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    @NonNull
    private LineDataSet generateLineDataStandardEqn3() {
        ArrayList<Entry> entries = new ArrayList<>();
        Log.e("TAG", "generateLineDataStandard: " + Arrays.toString(cvValues[0]));
        entries.add(new Entry(0, sampleData[0].getConcentration() / 4));
        entries.add(new Entry(cvValues[0][cvValues[0].length - 1] / 13, sampleData[0].getConcentration() / 4));
        entries.add(new Entry(11 * cvValues[0][cvValues[0].length - 1] / 13, Math.min(100, sampleData[0].getConcentration() * 2)));
        entries.add(new Entry(cvValues[0][cvValues[0].length - 1], Math.min(100, sampleData[0].getConcentration() * 2)));
        entries.add(new Entry(cvValues[0][cvValues[0].length - 1], Math.min(100, sampleData[0].getConcentration() * 2)));
        LineDataSet set = new LineDataSet(entries, "Line DataSet 3");
        set.setColor(Color.rgb(31, 136, 222));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    @NonNull
    private LineDataSet generateLineDataStandardEqn2() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, sampleData[0].getConcentration() / 4));
        entries.add(new Entry(cvValues[0][cvValues[0].length - 1] / 13, sampleData[0].getConcentration() / 4));
        entries.add(new Entry(11 * cvValues[0][cvValues[0].length - 1] / 13, Math.min(100, sampleData[0].getConcentration())));
        entries.add(new Entry(cvValues[0][cvValues[0].length - 1], Math.min(100, sampleData[0].getConcentration())));
        entries.add(new Entry(cvValues[0][cvValues[0].length - 1], Math.min(100, sampleData[0].getConcentration())));
        LineDataSet set = new LineDataSet(entries, "Line DataSet 2");
        set.setColor(Color.rgb(31, 136, 222));
        set.setLineWidth(1f);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    @NonNull
    private LineDataSet generateLineDataStandardEqn1() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0));
        entries.add(new Entry(cvValues[0][cvValues[0].length - 1] * 1.25f, sampleData[0].getConcentration()));
        entries.add(new Entry(cvValues[0][cvValues[0].length - 1] * 1.25f, sampleData[0].getConcentration()));
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

        ArrayList<Integer> colors = AppFunctions.getDistinctColors(cvValues[0].length);
        ArrayList<LineDataSet> sets = new ArrayList<>();
        for (int i = 0; i < cvValues[0].length; i++) {
            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(cvValues[0][i], 0f));
            entries.add(new Entry(cvValues[0][i], 110f));
            LineDataSet set = new LineDataSet(entries, "CV:" + (char) ('A' + i));
            set.setColor(colors.get(i));
//            set.setColor(Color.rgb(199, 78, 54));
            set.setLineWidth(1f);
            set.setDrawCircles(false);
            set.setMode(LineDataSet.Mode.LINEAR);
            set.setDrawValues(false);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            sets.add(set);
        }
        return sets;
    }

    @Override
    public void onProgressChanged(@NonNull SeekBar seekBar, int progress, boolean fromUser) {
        ArrayList<ILineDataSet> data = new ArrayList<>();
        switch (seekBar.getId()) {
            case R.id.sbHold:
                hold = progress;
                tvHold.setText(progress+"");
                break;
            case R.id.sbPercentageX:
                percentageX = progress;
                tvPercentX.setText(progress+"%");
                break;
        }
        data.add(generateLineDataStandardEqn4());
        data.addAll(generateVerticalLineDataStandard());
        chart.getXAxis().setAxisMaximum(data.get(0).getXMax());
        LineData lineData = new LineData(data);
        chart.setData(lineData);
        chart.fitScreen();
        chart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}