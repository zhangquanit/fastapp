package com.fastapp;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.base.Constant;
import com.android.base.event.PushEvent;
import com.android.base.ui.SimpleFrag;
import com.android.base.ui.SimpleFragAct;
import com.android.base.widget.StatusBar;

import org.greenrobot.eventbus.EventBus;


/**
 * @author 张全
 */
public class TestFrag extends SimpleFrag {
    public static void start(Context ctx) {
        SimpleFragAct.start(ctx, new SimpleFragAct.SimpleFragParam("TestFrag", TestFrag.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        StatusBar.setStatusBar(mContext, true, getTitleBar());

        findViewById(R.id.showDialog).setVisibility(View.GONE);
        findViewById(R.id.http).setVisibility(View.GONE);
        TextView textView = findViewById(R.id.testFrag);
        textView.setText("发送Event");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new PushEvent(Constant.Event.LOGIN_SUCCESS,"登录成功"));
            }
        });

    }
}
