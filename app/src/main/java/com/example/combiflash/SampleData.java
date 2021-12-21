package com.example.combiflash;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class SampleData {
    private float concentration;
    private float[] rfValues;

    public SampleData(float concentration, float[] rfValues) {
        this.concentration = concentration;
        this.rfValues = rfValues;
    }

    public float getConcentration() {
        return concentration;
    }

    public void setConcentration(float concentration) {
        this.concentration = concentration;
    }

    public float[] getRfValues() {
        return rfValues;
    }

    public void setRfValues(float[] rfValues) {
        this.rfValues = rfValues;
    }

    public float[] getSortedCvValues(){
        float[] cvValues=new float[rfValues.length];
        for(int i=0;i<cvValues.length;i++){
            cvValues[i]=1.0f/rfValues[i];
        }
        Arrays.sort(cvValues);
        return cvValues;
    }

    @NonNull
    @Override
    public String toString() {
        return "Concentration = "+concentration+"\nRfValues = "+ Arrays.toString(rfValues) +"\n\n";
    }
}
