package com.android.base.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * DialogFragment 基类
 *
 * @author hmy
 */
public class BaseDialogFragment extends DialogFragment {

    private boolean mIsLoading = false;

    protected void setNoTitle() {
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
    }

    @Override
    public void dismiss() {
        dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mIsLoading = false;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (mIsLoading) {
            return;
        }
        try {
            FragmentTransaction ft = manager.beginTransaction();
            if (!this.isAdded() && null == manager.findFragmentByTag(tag)) {
                ft.add(this, tag);
            } else {
                ft.show(this);
            }
            mIsLoading = true;
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int show(FragmentTransaction ft, String tag) {
        if (mIsLoading) {
            return -1;
        }
        try {
            if (!this.isAdded()) {
                ft.add(this, tag);
            } else {
                ft.show(this);
            }
            mIsLoading = true;
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
