package com.android.base.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import com.android.util.LContext;
import com.fastapp.R;


/**
 * 底部弹框(弹框高度最大为屏幕高度的70%)
 *
 * @author 张全
 */
public class BottomInDialog extends Dialog {
    public BottomInDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //弹框高度最大为屏幕高度的70%
        int height = (int) (LContext.screenHeight * 0.7);

        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = height;
        lp.windowAnimations = R.style.AnimBottom;
        lp.gravity = Gravity.BOTTOM;
        win.setAttributes(lp);
        win.setBackgroundDrawableResource(android.R.color.transparent);
        setCanceledOnTouchOutside(false); // 点击屏幕Dialog以外的地方是否消失
    }
}
