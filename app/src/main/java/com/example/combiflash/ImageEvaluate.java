package com.example.combiflash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Stack;

public class ImageEvaluate extends AppCompatActivity implements View.OnTouchListener {
    ImageView iv_capture;
    AppCompatButton addBtn, minusBtn, proceedBtn;
    ConstraintLayout parentLayout, viewLineUpLayout;
    View upView, bottomView;
    private boolean isStartedForResult;
    private int sampleIndex = -1;
    int lockClicked = 0, pos;
    float dy, valueMoveUp, concentration;
    Stack<View> viewStack;
    HashMap<View, Handler> rfLockHandlersMap;
    ViewGroup view;
    private static final DecimalFormat dfZero = new DecimalFormat("0.00");

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_image_evaluate);
        isStartedForResult = getIntent().getBooleanExtra(getResources().getString(R.string.isStartedForResultKey), false);
        if (isStartedForResult)
            sampleIndex = getIntent().getIntExtra(getResources().getString(R.string.sampleIndexKey), -1);
        String path = getIntent().getStringExtra("img_path");
        concentration = getIntent().getFloatExtra(getResources().getString(R.string.concentrationKey), 0);
        valueMoveUp = getIntent().getFloatExtra("valueUpMove", 0);
        pos = getIntent().getIntExtra("pos", 0);
        Log.e("pos2", String.valueOf(pos));
        upView = findViewById(R.id.viewLineUp);
        bottomView = findViewById(R.id.viewLine);
        iv_capture = findViewById(R.id.iv_capture);
        parentLayout = findViewById(R.id.imgLayout);
        viewLineUpLayout = findViewById(R.id.layout);
        viewLineUpLayout.setY(valueMoveUp);

        addBtn = findViewById(R.id.addBtn);
        minusBtn = findViewById(R.id.minusBtn);
        proceedBtn = findViewById(R.id.btnProceed);
        iv_capture.setImageURI(Uri.parse(path));
        view = findViewById(android.R.id.content);
        viewStack = new Stack<>();
        rfLockHandlersMap = new HashMap<>();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addViewItem();
            }
        });
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewStack.empty()) {
                    View v = viewStack.peek();
                    parentLayout.removeView(v);
                    viewStack.pop();
                    clearLockTimerRunnable(v);
                    rfLockHandlersMap.remove(v);
                }
            }
        });
        proceedBtn.setOnClickListener(v -> {
            float[] rfValues = new float[viewStack.size()];
            for (int i = 0; i < rfValues.length; i++) {
                View lineView = viewStack.pop();
                int[] l1 = new int[2];
                int[] l2 = new int[2];
                bottomView.getLocationOnScreen(l1);
                float btl = (float) l1[1] - (((float) bottomView.getHeight()) / (float) 2) - getResources().getDimensionPixelSize(R.dimen.pad);
                lineView.getLocationOnScreen(l2);
                float k = (concentration * (l2[1] - pos + getResources().getDimensionPixelSize(R.dimen.pad2))) /
                        (btl - pos + getResources().getDimensionPixelSize(R.dimen.pad2));
                rfValues[i] = (concentration - k) / concentration;
            }
            if (isStartedForResult) {
                Intent backwardIntent = new Intent();
                backwardIntent.putExtra(getResources().getString(R.string.sampleIndexKey), sampleIndex);
                backwardIntent.putExtra(getResources().getString(R.string.concentrationKey), concentration);
                backwardIntent.putExtra(getResources().getString(R.string.rfArrayKey), rfValues);
                setResult(RESULT_OK, backwardIntent);
                finish();
            } else {
                Intent graphIntent = new Intent(ImageEvaluate.this, GraphActivity.class);
                graphIntent.putExtra(getResources().getString(R.string.modeKey), getResources().getString(R.string.modeStandard));
                graphIntent.putExtra(getResources().getString(R.string.concentrationKey), concentration);
                graphIntent.putExtra(getResources().getString(R.string.rfArrayKey), rfValues);
                startActivity(graphIntent);
                finish();
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.layout) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dy = view.getY() - motionEvent.getRawY();
                    clearLockTimerRunnable(view);
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.setY(motionEvent.getRawY() + dy);
                    clearLockTimerRunnable(view);
                    break;
                case MotionEvent.ACTION_UP:
                    clearLockTimerRunnable(view);
                    setLockTimerRunnable(view);
                    break;
                default:
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private void lockLayout(View layout) {
        ImageView lockBtn = layout.findViewById(R.id.lockBtn);
        TextView textHeight = layout.findViewById(R.id.height);
        View line = layout.findViewById(R.id.viewLine);
        lockClicked = 1;
        int l1[] = new int[2];
        int l2[] = new int[2];
        bottomView.getLocationOnScreen(l1);
        float btl = (float) l1[1] - (((float) bottomView.getHeight()) / (float) 2) - getResources().getDimensionPixelSize(R.dimen.pad);
        layout.getLocationOnScreen(l2);
        float k = (float) (((float) (concentration * (l2[1] - pos + getResources().getDimensionPixelSize(R.dimen.pad2)))) /
                ((float) (btl - pos + getResources().getDimensionPixelSize(R.dimen.pad2))));
        float actualDistance = (concentration - k) / concentration;
        textHeight.setText("Rf " + dfZero.format(actualDistance));
        textHeight.setVisibility(View.VISIBLE);
        lockBtn.setImageDrawable(getResources().getDrawable(R.drawable.lock2));
        layout.setEnabled(false);
        line.setBackgroundResource(R.drawable.line_locked);
    }

    private void unlockLayout(View layout) {
        TextView textHeight = layout.findViewById(R.id.height);
        View line = layout.findViewById(R.id.viewLine);
        ImageView lockBtn = layout.findViewById(R.id.lockBtn);
        line.setBackgroundResource(R.drawable.line2);
        lockClicked = 0;
        textHeight.setVisibility(View.INVISIBLE);
        lockBtn.setImageDrawable(getResources().getDrawable(R.drawable.unlock));
        layout.setEnabled(true);
        layout.setOnTouchListener(ImageEvaluate.this);
    }

    private void clearLockTimerRunnable(View view) {
        rfLockHandlersMap.get(view).removeCallbacksAndMessages(null);
    }

    private void setLockTimerRunnable(View view) {
        Runnable myRunnable = () -> lockLayout(view);
        rfLockHandlersMap.get(view).postDelayed(myRunnable, 2000);
    }

    public void addViewItem() {
        View layout = getLayoutInflater().inflate(R.layout.line_layout, view, false);
        viewStack.push(layout);
        rfLockHandlersMap.put(layout, new Handler());
        ImageView lockBtn = layout.findViewById(R.id.lockBtn);

        lockBtn.setImageDrawable(getResources().getDrawable(R.drawable.unlock));

        layout.setOnTouchListener(this);
        lockBtn.setOnClickListener(it -> {
            if (lockClicked == 0) {
                lockLayout(layout);
                clearLockTimerRunnable(layout);
            } else {
                unlockLayout(layout);
                clearLockTimerRunnable(layout);
                setLockTimerRunnable(layout);
            }
        });
        parentLayout.addView(layout);
    }
}