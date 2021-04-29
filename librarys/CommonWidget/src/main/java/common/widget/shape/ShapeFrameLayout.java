package common.widget.shape;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 支持shape背景的FrameLayout
 * @author zhangquan
 */
public class ShapeFrameLayout extends FrameLayout {
    public ShapeFrameLayout(Context context) {
        this(context, null);
    }

    public ShapeFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setBackground(ShapeHelper.parseAttr(context, attrs));
    }
}
