package common.widget.shape.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.Nullable;

import common.widget.shape.ShapeAttrParser;

/**
 * 支持shape背景的EditText
 *
 * @author zhangquan
 */
@SuppressLint("AppCompatCustomView")
public class ShapeEditText extends EditText {

    public ShapeEditText(Context context) {
        this(context, null);
    }

    public ShapeEditText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        ShapeAttrParser.parseAttr(context, attrs,this);
    }
}
