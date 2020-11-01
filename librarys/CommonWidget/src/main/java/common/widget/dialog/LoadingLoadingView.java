package common.widget.dialog;

import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import common.widget.R;

/**
 * @author 张全
 */
public class LoadingLoadingView extends DialogView {
    private ImageView mLoadingView;
    private TextView mLoadingTxt;
    private String loadingTxt;
    private ValueAnimator anim;

    public LoadingLoadingView(Context ctx) {
        super(ctx);
    }

    public LoadingLoadingView(Context ctx, String loadingTxt) {
        super(ctx);
        this.loadingTxt = loadingTxt;
    }

    @Override
    protected void initView(View view) {
        mLoadingView = (ImageView) findViewById(R.id.loadingbar);
        mLoadingTxt = (TextView) findViewById(R.id.loadingbar_txt);
        if (!TextUtils.isEmpty(loadingTxt)) {
            mLoadingTxt.setText(loadingTxt);
        }
    }

    public void startAnimal() {
        if (mLoadingView != null && anim != null) {
            anim.start();
        }
    }

    public void stop() {
        if (mLoadingView != null && anim != null) {
            anim.cancel();
            anim = null;
        }
    }

    public void setText(String text) {
        mLoadingTxt.setText(text);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_loading;
    }

}
