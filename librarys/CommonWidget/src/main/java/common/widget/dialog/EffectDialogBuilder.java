package common.widget.dialog;

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import common.widget.R;

/**
 * 特效Dialog
 *
 * @author 张全
 */
public class EffectDialogBuilder {

    private CustomDialog mDialog;

    private int mDuration = -1;
    private static long DISMISS_DURATION = 2 * 1000;
    protected static Handler mHandler = null;

    public EffectDialogBuilder(Context ctx) {
        this(ctx, R.style.Dialog_untran);
    }

    public EffectDialogBuilder(Context ctx, int style) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }

        mDialog = new CustomDialog(ctx, style);
        mDialog.setCancelable(true);
        // 显示动画
        mDialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
//                // mDialog.getContentView().setVisibility(View.VISIBLE);
//                if (null == animator) {
//                    return;
//                }
//
//                if (mDuration != -1) {
//                    animator.setDuration(Math.abs(mDuration));
//                }
//                animator.start(mDialog.getRootView());
            }
        });
    }

//    public EffectDialogBuilder setEffect(Effectstype type) {
//        animator = type.getAnimator();
//        return this;
//    }
//
//    public EffectDialogBuilder setEffect(Effectstype type,
//                                         Animator.AnimatorListener listener) {
//        animator = type.getAnimator();
//        animator.getAnimatorSet().addListener(listener);
//        return this;
//    }
//
//    public EffectDialogBuilder setEffect(BaseEffects effect) {
//        this.animator = effect;
//        return this;
//    }
//
//    public EffectDialogBuilder setEffect(BaseEffects effect,
//                                         Animator.AnimatorListener listener) {
//        this.animator = effect;
//        this.animator.getAnimatorSet().addListener(listener);
//        return this;
//    }
//
//    public EffectDialogBuilder setEffectDuration(int duration) {
//        this.mDuration = duration;
//        return this;
//    }
//
//    public EffectDialogBuilder setAnimatorListener(Animator.AnimatorListener listener) {
//        if (null != animator) {
//            animator.getAnimatorSet().addListener(listener);
//        }
//        return this;
//    }
//
//    public EffectDialogBuilder setAutoDismissTimemills(long diss) {
//        DISMISS_DURATION = diss;
//        return this;
//    }
//
//    public EffectDialogBuilder setContentView(int resId, Context context) {
//        setContentView(View.inflate(context, resId, null));
//        return this;
//    }

    public EffectDialogBuilder setContentView(View view) {
        mDialog.setCustomView(view);
        return this;
    }

    public EffectDialogBuilder setContentView(DialogView dialogView) {
        dialogView.setEffecctDialogBuilder(this);
        mDialog.setCustomView(dialogView.getContentView());
        return this;
    }

    public EffectDialogBuilder setCancelableOnTouchOutside(boolean cancelable) {
        mDialog.setCanceledOnTouchOutside(cancelable);
        return this;
    }

    public EffectDialogBuilder setCancelable(boolean cancelable) {
        mDialog.setCancelable(cancelable);
        return this;
    }

    public EffectDialogBuilder setOnDismissListener(OnDismissListener listener) {
        mDialog.setOnDismissListener(listener);
        return this;
    }

    public EffectDialogBuilder setOnCancelListener(OnCancelListener listener) {
        mDialog.setOnCancelListener(listener);
        return this;
    }

    public View getRootView() {
        return mDialog.getRootView();
    }

    public Dialog show() {
        return show(false);
    }

    public Dialog show(boolean isAutoDismiss) {
        return show(isAutoDismiss, DISMISS_DURATION);
    }

    public Dialog show(boolean isAutoDismiss, long dissmiss_duration) {
        try {
            if (!isContextAvailable()) {
                return mDialog;
            }
            mDialog.show();

            // 自动隐藏dialog
            if (isAutoDismiss) {
                mHandler.postDelayed(runnable, dissmiss_duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mDialog;
    }

    public void dismiss() {
        try {
            if (!isContextAvailable()) {
                return;
            }

            if (isShowing()) {
                mDialog.dismiss();
            }
            mHandler.removeCallbacks(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isShowing() {
        return null != mDialog ? mDialog.isShowing() : false;
    }

    private boolean isContextAvailable() {
        Context context = mDialog.getContext();
        if (null != context && context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return false;
            }
        }
        return true;
    }

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            dismiss();
        }
    };
}
