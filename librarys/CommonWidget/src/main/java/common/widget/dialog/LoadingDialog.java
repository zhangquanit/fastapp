package common.widget.dialog;

import android.content.Context;

import common.widget.R;

/**
 * 加载进度框
 *
 * @author zhangquan
 */
public class LoadingDialog {
    private EffectDialogBuilder builder;
    private LoadingLoadingView dialogView;

    public LoadingDialog(Context ctx, String text) {
        this(ctx, text, false);
    }

    public LoadingDialog(Context ctx, String text, boolean cancelable) {
        builder = new EffectDialogBuilder(ctx, R.style.Dialog_tran);
        builder.setCancelableOnTouchOutside(cancelable);
        builder.setCancelable(cancelable);
        dialogView = new LoadingLoadingView(
                ctx, text);
        builder.setContentView(dialogView);
    }

    public LoadingDialog show() {
//        dialogView.startAnimal();
        builder.show();
        return this;
    }

    public boolean isShowing() {
        return builder.isShowing();
    }

    public LoadingDialog setCancelableOnTouchOutside(boolean cancelable) {
        builder.setCancelableOnTouchOutside(cancelable);
        return this;
    }

    public LoadingDialog setCancelable(boolean cancelable) {
        builder.setCancelable(cancelable);
        return this;
    }

    public LoadingDialog dismiss() {
        dialogView.stop();
        builder.dismiss();
        return this;
    }

    public static LoadingDialog showDialog(Context context, String text) {
        return new LoadingDialog(context, text).show();
    }

    public static LoadingDialog showBackCancelableDialog(Context context, String text) {
        return new LoadingDialog(context, text).setCancelableOnTouchOutside(false).setCancelable(true).show();
    }

    public static LoadingDialog showCancelableDialog(Context context, String text) {
        return new LoadingDialog(context, text, true).show();
    }
}
