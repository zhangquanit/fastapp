package common.widget.shape;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import androidx.annotation.Nullable;

/**
 * 支持shape背景的TextView
 *
 * @author zhangquan
 */
@SuppressLint("AppCompatCustomView")
public class ShapeButton extends Button {

    public ShapeButton(Context context) {
        this(context, null);
    }

    public ShapeButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        setBackground(ShapeHelper.parseAttr(context, attrs));
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}
