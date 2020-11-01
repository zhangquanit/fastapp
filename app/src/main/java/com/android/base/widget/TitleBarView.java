package com.android.base.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.blankj.utilcode.util.SizeUtils;
import com.fastapp.R;

/**
 * 通用标题栏
 */
public final class TitleBarView extends RelativeLayout implements
        OnClickListener {

    // 标题文本控件
    public TextView mTitleTextView;
    // 标题左边文字
    public TextView mLeftTextView;
    // 左右侧按钮及右侧旁边按钮
    public ImageView mLeftButton, mRightButton, mSubjoinButton;
    // 右边文字
    public TextView mRightTxtView, mRightSubTextView;

    public TitleBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(getContext());
    }

    public TitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(getContext());
    }

    public TitleBarView(Context context) {
        super(context);
        initView(getContext());
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.common_titlebar, this);
        // 标题
        mTitleTextView = findViewById(R.id.title_text_view);

        // 左边控件
        mLeftButton = findViewById(R.id.title_left_button);
        mLeftTextView = findViewById(R.id.title_left_txt);

        // 右边按钮
        mRightButton = findViewById(R.id.title_right_button);
        mSubjoinButton = findViewById(R.id.title_subjoin_button);
        // 右边文字
        mRightTxtView = findViewById(R.id.title_righttxt);
        mRightSubTextView = findViewById(R.id.title_righttxt_sub);

        setLeftBtnDrawable(R.drawable.back);
    }

    /**
     * 设置文字颜色
     *
     * @param color
     */
    public TitleBarView setTitleTextColor(@ColorRes int color) {
        mTitleTextView.setTextColor(getResources().getColor(color));
        return this;
    }

    /**
     * 设置标题文字
     *
     * @param text
     */
    public TitleBarView setTitleText(String text) {
        mTitleTextView.setText(text);
        return this;
    }

    public TitleBarView setLeftText(@StringRes int text) {
        setLeftText(getContext().getString(text));
        return this;
    }

    public TitleBarView setLeftText(String text) {
        mLeftTextView.setText(text);
        mLeftTextView.setVisibility(View.VISIBLE);
//		mLeftButton.setVisibility(View.GONE);
        return this;
    }

    /**
     * 设置字体
     *
     * @param typeface
     */
    public TitleBarView setTitleTypeface(final Typeface typeface) {
        mTitleTextView.setTypeface(typeface);
        return this;
    }

    /**
     * 使用资源设置标题
     *
     * @param resId
     */
    public TitleBarView setTitleText(@StringRes int resId) {
        String text = getResources().getString(resId);
        mTitleTextView.setText(text);
        return this;
    }

    /**
     * 获取当前标题文字
     *
     * @return
     */
    public String getTitle() {
        return mTitleTextView.getText().toString();
    }

    /**
     * 设置左侧按钮监听器
     *
     * @param listener
     */
    public TitleBarView setOnLeftBtnClickListener(OnClickListener listener) {
        mLeftButton.setOnClickListener(listener);
        return this;
    }

    public TitleBarView setOnLeftTxtClickListener(OnClickListener listener) {
        mLeftTextView.setOnClickListener(listener);
        return this;
    }

    /**
     * 设置右侧按钮监听器
     *
     * @param listener
     */
    public TitleBarView setOnRightBtnClickListener(OnClickListener listener) {
        mRightButton.setOnClickListener(listener);
        return this;
    }

    public TitleBarView setOnRightTxtClickListener(OnClickListener listener) {
        mRightTxtView.setOnClickListener(listener);
        return this;
    }

    /**
     * 设置附加按钮监听器
     *
     * @param listener
     */
    public TitleBarView setOnSubjoinBtnClickListener(OnClickListener listener) {
        mSubjoinButton.setOnClickListener(listener);
        return this;
    }

    public TitleBarView setCompoundDrawable(TextView textView, int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        setCompoundDrawable(textView, drawable);
        return this;
    }

    public TitleBarView setCompoundDrawable(TextView textView, Drawable drawable) {
        setCompoundDrawable(textView, drawable, 4);
        return this;
    }

    public TitleBarView setCompoundDrawable(TextView textView, int resId, int drawablePadding) {
        Drawable drawable = getResources().getDrawable(resId);
        setCompoundDrawable(textView, drawable, drawablePadding);
        return this;
    }

    public TitleBarView setCompoundDrawable(TextView textView, Drawable drawable, int drawablePadding) {
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        textView.setCompoundDrawables(drawable, null, null, null);
        textView.setCompoundDrawablePadding(SizeUtils.dp2px(drawablePadding));
        textView.setGravity(Gravity.CENTER_VERTICAL);
        return this;
    }

    /**
     * 设置左侧按钮资源图片
     *
     * @param resId
     */
    public TitleBarView setLeftBtnDrawable(@DrawableRes int resId) {
        mLeftButton.setVisibility(View.VISIBLE);
        mLeftButton.setImageResource(resId);
        return this;
    }

    public TitleBarView setLeftBtnWhiteColor() {
        mLeftButton.setColorFilter(Color.WHITE);
        return this;
    }

    /**
     * 设置左侧按钮资源图片
     *
     * @param resId
     */
    public TitleBarView setRightBtnDrawable(@DrawableRes int resId) {
        mRightButton.setVisibility(View.VISIBLE);
        mRightButton.setImageResource(resId);
        return this;
    }

    /**
     * 设置附加按钮资源图片
     *
     * @param resId
     */
    public TitleBarView setSubjoinBtnDrawable(@DrawableRes int resId) {
        mSubjoinButton.setVisibility(View.VISIBLE);
        mSubjoinButton.setImageResource(resId);
        return this;
    }

    public TitleBarView setRightText(String text) {
        mRightTxtView.setVisibility(View.VISIBLE);
        mRightTxtView.setText(text);
        return this;
    }

    public TitleBarView setRightTextColor(@ColorRes int color) {
        mRightTxtView.setTextColor(getResources().getColor(color));
        return this;
    }

    public TitleBarView setRightSubText(String text) {
        mRightSubTextView.setVisibility(View.VISIBLE);
        mRightSubTextView.setText(text);
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left_button
                && getContext() instanceof Activity) {
            hideSoftInput();
        }
    }

    public void hideSoftInput() {
        InputMethodManager inputManger = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManger.hideSoftInputFromWindow(getWindowToken(), 0);
    }
}
