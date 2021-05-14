package com.fastapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.base.ui.BaseActivity;

public class ActivityTwo extends BaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        findViewById(R.id.testFrag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("param", "data from ActivityTwo");
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

}
