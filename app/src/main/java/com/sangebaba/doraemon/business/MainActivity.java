package com.sangebaba.doraemon.business;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.sangebaba.doraemon.business.control.Doraemon;

public class MainActivity extends Activity {

    private Doraemon mDoraemon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDoraemon = Doraemon.getInstance(this);

        findViewById(R.id.bt_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDoraemon.startASR();
            }
        });
    }
}
