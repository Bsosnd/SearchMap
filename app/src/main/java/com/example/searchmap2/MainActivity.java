package com.example.searchmap2;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    Button btn_setPlace;//장소직접선택

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_setPlace=findViewById(R.id.btn_setPlace);

        btn_setPlace.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.btn_setPlace:
                Intent intent=new Intent(this, SearchMapActivity.class);
                startActivity(intent);
        }
    }
}
