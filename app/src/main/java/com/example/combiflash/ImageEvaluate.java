package com.example.combiflash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Stack;

public class ImageEvaluate extends AppCompatActivity implements View.OnTouchListener{
    ImageView iv_capture;
    AppCompatButton addBtn,minusBtn,proceedBtn;
    ConstraintLayout parentLayout,viewLineUpLayout;
    View upView,bottomView;
    private boolean isStartedForResult;
    private int sampleIndex=-1;
    int lockClicked=0,pos;
    float dy,valueMoveUp,height_in_cm;
    Stack<View>viewStack;
    ViewGroup view;
    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_image_evaluate);
        isStartedForResult=getIntent().getBooleanExtra(getResources().getString(R.string.isStartedForResultKey),false);
        if(isStartedForResult)
            sampleIndex=getIntent().getIntExtra(getResources().getString(R.string.sampleIndexKey),-1);
        String path=getIntent().getStringExtra("img_path");
        height_in_cm=  getIntent().getFloatExtra("height_in_cm",0);
        valueMoveUp=getIntent().getFloatExtra("valueUpMove",0);
        pos=getIntent().getIntExtra("pos",0);
        Log.e("pos2",String.valueOf(pos));
        upView=findViewById(R.id.viewLineUp);
        bottomView=findViewById(R.id.viewLine);
        iv_capture=findViewById(R.id.iv_capture);
        parentLayout=findViewById(R.id.imgLayout);
        viewLineUpLayout=findViewById(R.id.layout);
        viewLineUpLayout.setY(valueMoveUp);

        addBtn=findViewById(R.id.addBtn);
        minusBtn=findViewById(R.id.minusBtn);
        proceedBtn=findViewById(R.id.btnProceed);
        iv_capture.setImageURI(Uri.parse(path));
        view=findViewById(android.R.id.content);
        viewStack=new Stack<>();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addViewItem();
            }
        });
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!viewStack.empty()){
                    View v=viewStack.peek();
                    parentLayout.removeView(v);
                    viewStack.pop();
                }
            }
        });
        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStartedForResult){
                    Intent intent=new Intent();
//                    intent.putExtra
                    setResult(RESULT_OK,intent);
                    finish();
                    // TODO: 08/12/21 just set the result to the required values
                }
                else{
                    // TODO: 08/12/21 if this activity was not started for result, then open the graph activity from here with all the requireds values(beacuse it is standard mode

                }
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(view.getId()==R.id.layout){
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    dy=view.getY()-motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.setY(motionEvent.getRawY() + dy);
                    break;
                default :
                    return false;
            }
            return true;
        }
        else {
            return false;
        }
    }

    public void addViewItem(){
        View layout = getLayoutInflater().inflate(R.layout.line_layout, view,false);
        viewStack.push(layout);
        ImageView lockBtn=layout.findViewById(R.id.lockBtn);
        TextView textHeight=layout.findViewById(R.id.height);
        lockBtn.setBackground(getResources().getDrawable(R.drawable.unlock));

        layout.setOnTouchListener(this);
        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lockClicked==0){
                    lockClicked=1;
                    int l1[]=new int[2];
                    int l2[]=new int[2];
                    bottomView.getLocationOnScreen(l1);
                    float btl=(float)l1[1]-(((float) bottomView.getHeight())/(float) 2)-getResources().getDimensionPixelSize(R.dimen.pad);
                    layout.getLocationOnScreen(l2);
                    float k=(float) (((float)(height_in_cm*(l2[1]-pos+getResources().getDimensionPixelSize(R.dimen.pad2))))/
                            ((float) (btl-pos+getResources().getDimensionPixelSize(R.dimen.pad2))));
                    float actualDistance=(height_in_cm-k)/height_in_cm;
                    textHeight.setText("Rf "+actualDistance);
                    textHeight.setVisibility(View.VISIBLE);
                    lockBtn.setBackground(getResources().getDrawable(R.drawable.lock2));
                    layout.setEnabled(false);
                }
                else{
                    lockClicked=0;
                    textHeight.setVisibility(View.INVISIBLE);
                    lockBtn.setBackground(getResources().getDrawable(R.drawable.unlock));
                    layout.setEnabled(true);
                    layout.setOnTouchListener(ImageEvaluate.this);
                }

            }
        });
        parentLayout.addView(layout);
    }
}