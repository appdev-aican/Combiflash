package com.example.combiflash;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ShowImgActivity extends AppCompatActivity implements View.OnTouchListener{
    private boolean isStartedForResult;
    private int sampleIndex=-1;
    private final int requestCode=1;
    ImageView iv_capture;
    View viewLineUp;
    ConstraintLayout layout;
    AppCompatButton setLengthBtn,alterLengthBtn,proceedBtn;
    float dy,valueMovement;
    float height_in_cm=0;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_img);
        getSupportActionBar().hide();
        isStartedForResult=getIntent().getBooleanExtra(getResources().getString(R.string.isStartedForResultKey),false);
        if(isStartedForResult)
            sampleIndex=getIntent().getIntExtra(getResources().getString(R.string.sampleIndexKey),-1);
        String path=getIntent().getStringExtra("img_path");
        iv_capture=findViewById(R.id.iv_capture);
        setLengthBtn=findViewById(R.id.setLengthBtn);
        alterLengthBtn=findViewById(R.id.alterLengthBtn);
        proceedBtn=findViewById(R.id.proceedBtn);
        layout=findViewById(R.id.layout);
        iv_capture.setImageURI(Uri.parse(path));
        viewLineUp=findViewById(R.id.viewLineUp);
        setLengthBtn.setVisibility(View.VISIBLE);
        alterLengthBtn.setVisibility(View.GONE);
        proceedBtn.setVisibility(View.GONE);
        setLengthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowImgActivity.this);
                builder.setTitle("Please enter the distance between the bottom and top lines");
                final View customLayout = getLayoutInflater().inflate(R.layout.edit_text_layout, null);
                builder.setView(customLayout);
                builder
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                        EditText editText = customLayout.findViewById(R.id.editText);
                                        if(editText.getText().toString().isEmpty()){
                                            Toast.makeText(ShowImgActivity.this, "Please enter any data", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            height_in_cm=Float.parseFloat(editText.getText().toString());
                                            alterLengthBtn.setVisibility(View.VISIBLE);
                                            setLengthBtn.setVisibility(View.GONE);
                                            proceedBtn.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        alterLengthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowImgActivity.this);
                builder.setTitle("Please enter the distance between the bottom and top lines");
                final View customLayout = getLayoutInflater().inflate(R.layout.edit_text_layout, null);
                builder.setView(customLayout);
                builder
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                EditText editText = customLayout.findViewById(R.id.editText);
                                if(editText.getText().toString().isEmpty()){
                                    Toast.makeText(ShowImgActivity.this, "Please enter any data", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    height_in_cm=Float.parseFloat(editText.getText().toString());
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] h=new int[2];
                layout.getLocationOnScreen(h);
                Log.e("pos1",String.valueOf(h[1]));
                Intent intent=new Intent(ShowImgActivity.this,ImageEvaluate.class);
                intent.putExtra("img_path",path);
                intent.putExtra("height_in_cm",height_in_cm);
                intent.putExtra("valueUpMove",valueMovement);
                intent.putExtra("pos",h[1]);
                if(isStartedForResult){
                    intent.putExtra(getResources().getString(R.string.isStartedForResultKey), true);
                    intent.putExtra(getResources().getString(R.string.sampleIndexKey), sampleIndex);
                    startActivityForResult(intent, requestCode);
                }
                else{
                    intent.putExtra(getResources().getString(R.string.isStartedForResultKey),false);
                    startActivity(intent);
                }
            }
        });
        layout.setOnTouchListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(view.getId()==layout.getId()){
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    dy=view.getY()-motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.setY(motionEvent.getRawY() + dy);
                    valueMovement=motionEvent.getRawY()+dy;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==this.requestCode&&resultCode==RESULT_OK){
            Intent intent=new Intent();
            // TODO: 10/12/21 put the info received in result to the backward intent
//            intent.putExtra()
            setResult(RESULT_OK,intent);
        }
        finish();
    }
}