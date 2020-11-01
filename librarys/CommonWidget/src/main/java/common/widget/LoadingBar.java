package common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 加载进度条
 *
 * @author 张全
 */
public class LoadingBar extends RelativeLayout {
    private ImageView iv_loading;
    private ProgressBar mProgressBar;
    private TextView tv_loading;
    private LoadingStatus status;

    public LoadingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LoadingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingBar(Context context) {
        super(context);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.common_loadingbar, this);
        iv_loading = (ImageView) findViewById(R.id.loading_img);
        tv_loading = (TextView) findViewById(R.id.loading_text);
        mProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
    }

    Animation getAnim() {
        RotateAnimation anim = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        anim.setDuration(500);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(Animation.RESTART);
        return anim;
    }

    /**
     * 设置加载状态
     *
     * @param status 加载状态
     */
    public void setLoadingStatus(LoadingStatus status) {
        setLoadingStatus(status, -1, null);
    }

    /**
     * 设置加载状态
     *
     * @param status 加载状态
     * @param imgRes 显示的图片
     */
    public void setLoadingStatus(LoadingStatus status, int imgRes) {
        setLoadingStatus(status, imgRes, null);
    }

    /**
     * 设置加载状态
     *
     * @param status 加载状态
     * @param text   显示的文字
     */
    public void setLoadingStatus(LoadingStatus status, String text) {
        setLoadingStatus(status, -1, text);
    }

    /**
     * 设置加载状态
     *
     * @param status 加载状态
     * @param imgRes 显示的图片
     * @param text   显示的文字
     */
    public void setLoadingStatus(LoadingStatus status, int imgRes, String text) {

        if (null != this.status && this.status == status) {
            return;
        }

        if (status == LoadingStatus.SUCCESS) {
            loadSuccess();
            return;
        }

        this.status = status;
        setVisibility(View.VISIBLE);
        tv_loading.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
//		iv_loading.setVisibility(View.GONE);
//		iv_loading.clearAnimation();

        // 设置文字
        String loadingText = null == text ? getContext().getString(status.text) : text;
        tv_loading.setText(loadingText);
        if (!TextUtils.isEmpty(loadingText)) {
            tv_loading.setVisibility(View.VISIBLE);
        }

        // 设置图片
        if (imgRes > 0) {
            iv_loading.setImageResource(imgRes);
            iv_loading.setVisibility(View.VISIBLE);
        } else {
//			iv_loading.setImageBitmap(null);
        }

        switch (status) {
            case START:// 加载中...
                mProgressBar.setVisibility(View.VISIBLE);
                iv_loading.setVisibility(View.GONE);
                break;
            case RELOAD:// 重新加载
                iv_loading.setVisibility(View.VISIBLE);
                break;
            case NOCONNECTION:// 无网络连接
                iv_loading.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:// 加载成功
                setVisibility(View.GONE);
                break;
            case EMPTY: // 无数据
                iv_loading.setVisibility(View.VISIBLE);
                break;
        }
    }

    public LoadingStatus getLoadingStatus() {
        return this.status;
    }

    /**
     * 是否正在加载
     *
     * @return
     */
    public boolean isLoading() {
        return getVisibility() == View.VISIBLE && null != status
                && (status == LoadingStatus.START);
    }

    /**
     * 加载成功
     */
    public void loadSuccess() {
        this.status = LoadingStatus.SUCCESS;
        setVisibility(View.GONE);
    }

    /**
     * 是否能够加载
     *
     * @return
     */
    public boolean canLoading() {
        if (null == status) return false;
        if (status == LoadingStatus.START || status == LoadingStatus.EMPTY || status == LoadingStatus.SUCCESS) {
            return false;
        }
        return true;
    }

    /**
     * 加载状态
     *
     * @author zhangquan
     */
    public enum LoadingStatus {
        START(R.string.loadingbar_start), SUCCESS(R.string.loadingbar_success), NOCONNECTION(
                R.string.loadingbar_noconnection), RELOAD(
                R.string.loadingbar_reload), EMPTY(R.string.loadingbar_empty);

        public int text;

        private LoadingStatus(int text) {
            this.text = text;
        }
    }

    // --------------------------------
    public void setTextView(TextView textView) {
        this.tv_loading = textView;
    }

    public void setImageView(ImageView imageView) {
        this.iv_loading = imageView;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.mProgressBar = progressBar;
    }
}
