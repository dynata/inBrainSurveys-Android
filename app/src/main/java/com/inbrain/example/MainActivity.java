package com.inbrain.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.inbrain.sdk.InBrain;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InBrain.init("external-web-client", "l3!9hrl*olsdfliw#4uJO*f^j4ow8");
        InBrain.setAppUserId("1234-1234-1234-0000");
    }

    public void showSurveys(View view) {
        InBrain.showSurveys(this);
    }
}
