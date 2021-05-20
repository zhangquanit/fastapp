package common.widget.shape.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import androidx.annotation.Nullable;

import common.widget.shape.ShapeAttrParser;

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
        ShapeAttrParser.parseAttr(context, attrs, this);
    }
}
