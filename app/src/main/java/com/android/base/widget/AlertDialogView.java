package com.android.base.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.fastapp.R;

import common.widget.dialog.DialogView;
import common.widget.dialog.EffectDialogBuilder;

/**
 * 通用弹出框界面
 * <p>example:</p>
 * <pre class="prettyprint">
 * AlertDialogView dialogView = new AlertDialogView(getActivity())
 * .setTitle("标题")
 * .setContent("文本内容文本内容文本内容文本内容")
 * .setSingleBtn("确定", new View.OnClickListener() );
 *
 * new EffectDialogBuilder(getActivity())
 * .setCancelable(false)
 * .setCancelableOnTouchOutside(false)
 * .setContentView(dialogView)
 * .show();
 * </pre>
 *
 * <p>设置显示2个按钮</p>
 * <pre class="prettyprint">
 *         AlertDialogView dialogView = new AlertDialogView(getActivity())
 *                 .setTitle("标题")
 *                 .setContent("文本内容文本内容文本内容文本内容")//
 *                 .setLeftBtn("取消")
 *                 .setRightBtn("确定", new View.OnClickListener() )
 *                 .setSingleBtn("确定", new View.OnClickListener());
 *
 *         new EffectDialogBuilder(getActivity())
 *                 .setCancelable(false)
 *                 .setCancelableOnTouchOutside(false)
 *                 .setContentView(dialogView)
 *                 .show();
 * </pre
 * @author 张全
 */
public class AlertDialogView extends DialogView implements OnClickListener {
    public TextView tv_content, tv_content2;
    private LinearLayout contentBar;
    private int titleGravity = Gravity.CENTER;
    private int contentGravity1 = Gravity.CENTER, contentGravity2 = Gravity.CENTER;
    private boolean showTitleDivider = true;
    private TextView btn_right, btn_left;
    private TextView btn_single;
    private String title;
    private AlertDialogClickListener clickListener;
    private OnClickListener mLeftClickListener;
    private OnClickListener mRightClickListener;
    private OnClickListener mSingleClickListener;

    private int leftBtnColor = -1;
    private int rightBtnColor = -1;

    private boolean mCancelable = true;
    private boolean mOutsideCancelable = true;

    private String content, content2, leftBtnText, rightBtnText, singleBtnText;
    private SpannableStringBuilder spanContent;

    public AlertDialogView(Context ctx) {
        super(ctx);
    }

    @Override
    protected void initView(View view) {
        contentBar = (LinearLayout) findViewById(R.id.dialog_content_bar);
        tv_content = (TextView) findViewById(R.id.dialog_content);
        tv_content2 = (TextView) findViewById(R.id.dialog_content2);
        tv_content.setGravity(contentGravity1);
        tv_content2.setGravity(contentGravity2);
        btn_left = (TextView) findViewById(R.id.btn_left);
        btn_right = (TextView) findViewById(R.id.btn_right);
        btn_single = (TextView) findViewById(R.id.btn_single);

        btn_left.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        btn_single.setOnClickListener(this);

        if (leftBtnColor != -1) {
            btn_left.setTextColor(leftBtnColor);
        }
        if (rightBtnColor != -1) {
            btn_right.setTextColor(rightBtnColor);
        }

        // 标题
        if (!TextUtils.isEmpty(title)) {
            findViewById(R.id.dialog_titlebar).setVisibility(View.VISIBLE);
            TextView tv_title = (TextView) findViewById(R.id.dialog_title);
            tv_title.setText(title);
            tv_title.setGravity(titleGravity);
        }

        // 内容
        if (!TextUtils.isEmpty(spanContent)) {
            tv_content.setText(spanContent);
        } else {
            tv_content.setText(content);
        }
        if (!TextUtils.isEmpty(content2)) {
            tv_content2.setVisibility(View.VISIBLE);
            tv_content2.setText(content2);
        }

        if (!TextUtils.isEmpty(singleBtnText)) {
            btn_single.setText(singleBtnText);
            btn_single.setVisibility(View.VISIBLE);
        } else {
            if (!TextUtils.isEmpty(leftBtnText)) {
                btn_left.setText(leftBtnText);
                btn_left.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(rightBtnText)) {
                btn_right.setText(rightBtnText);
                btn_right.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_alert;
    }

    public AlertDialogView setClickListener(AlertDialogClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public AlertDialogView setTitle(int title) {
        return setTitle(getContext().getString(title));
    }

    public AlertDialogView setTitle(String title) {
        return setTitle(title, Gravity.CENTER);
    }

    public AlertDialogView setTitle(String title, int gravity) {
        this.title = title;
        this.titleGravity = gravity;
        return this;
    }

    public AlertDialogView setContent(int text) {
        return setContent(getContext().getString(text));
    }

    public AlertDialogView setContent(String text) {
        return setContent(text, Gravity.CENTER);
    }

    public AlertDialogView setContent(SpannableStringBuilder text){
        return setContent(text, Gravity.CENTER);
    }

    public AlertDialogView setContent(String text, int gravity) {
        this.content = text;
        this.contentGravity1 = gravity;
        return this;
    }

    public AlertDialogView setContent(SpannableStringBuilder text, int gravity) {
        this.spanContent = text;
        this.contentGravity1 = gravity;
        return this;
    }

    public AlertDialogView setContent2(int text) {
        return setContent2(getContext().getString(text));
    }

    public AlertDialogView setContent2(String text) {
        return setContent2(text, Gravity.CENTER);
    }

    public AlertDialogView setContent2(String text, int gravity) {
        this.content2 = text;
        this.contentGravity2 = gravity;
        return this;
    }

    public AlertDialogView showTitleDivider(boolean show) {
        this.showTitleDivider = show;
        return this;
    }

    public AlertDialogView setSingleBtn(String text) {
        setSingleBtn(text, null);
        return this;
    }

    public AlertDialogView setSingleBtn(String text, OnClickListener l) {
        this.singleBtnText = text;
        this.mSingleClickListener = l;
        return this;
    }

    public AlertDialogView setLeftBtn(String leftBtnTxt) {
        setLeftBtn(leftBtnTxt, null);
        return this;
    }

    public AlertDialogView setLeftBtn(String leftBtnTxt, OnClickListener l) {
        this.leftBtnText = leftBtnTxt;
        this.mLeftClickListener = l;
        return this;
    }

    public AlertDialogView setLeftBtnColor(int leftBtnColor) {
        this.leftBtnColor = leftBtnColor;
        return this;
    }

    public AlertDialogView setRightBtnColor(int rightBtnColor) {
        this.rightBtnColor = rightBtnColor;
        return this;
    }

    public AlertDialogView setRightBtn(String rightBtnTxt) {
        setRightBtn(rightBtnTxt, null);
        return this;
    }

    public AlertDialogView setRightBtn(String rightBtnTxt, OnClickListener l) {
        this.rightBtnText = rightBtnTxt;
        this.mRightClickListener = l;
        return this;
    }

    public AlertDialogView setCancelable(boolean cancelable) {
        this.mCancelable = cancelable;
        return this;
    }

    public AlertDialogView setCancelableOnTouchOutside(boolean outsideCancelable) {
        this.mOutsideCancelable = outsideCancelable;
        return this;
    }

    public Dialog show() {
        return new EffectDialogBuilder(getContext())
                .setCancelable(mCancelable)
                .setCancelableOnTouchOutside(mOutsideCancelable)
                .setContentView(this)
                .show();

    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v == btn_left) {
            if (null != mLeftClickListener) {
                mLeftClickListener.onClick(v);
            } else if (null != clickListener) {
                clickListener.onLeftClick();
            }
        } else if (v == btn_right) {
            if (null != mRightClickListener) {
                mRightClickListener.onClick(v);
            } else if (null != clickListener) {
                clickListener.onRightClick();
            }

        } else if (v == btn_single) {
            if (null != mSingleClickListener) {
                mSingleClickListener.onClick(v);
            } else if (null != clickListener) {
                clickListener.onSingleClick();
            }
        }
    }

    public interface AlertDialogClickListener {
        void onLeftClick();

        void onRightClick();

        void onSingleClick();
    }

}
