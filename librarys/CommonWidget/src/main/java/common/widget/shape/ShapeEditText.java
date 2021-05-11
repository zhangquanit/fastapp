package common.widget.shape;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

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
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        setBackground(ShapeHelper.parseAttr(context, attrs));
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}
