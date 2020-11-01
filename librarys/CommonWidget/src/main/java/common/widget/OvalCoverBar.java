package common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * 圆弧镂空
 */
public class OvalCoverBar extends View {
    // 画圆环的画笔
    private Paint paint;

    private RectF oval;
    private RectF clipRect;
    private float offset;

    public OvalCoverBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context ctx) {

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);

        offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                ctx.getResources().getDisplayMetrics());
        setLayerType(LAYER_TYPE_SOFTWARE, new Paint());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        oval = new RectF(-offset, 0, getMeasuredWidth() + offset, getMeasuredHeight());
        clipRect = new RectF(0, oval.height() / 2.0f, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);

        canvas.clipRect(clipRect);

        canvas.drawRect(oval, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        canvas.drawOval(oval, paint);

    }

    public void setColor(String color) {
        paint.setColor(Color.parseColor(color));
        invalidate();
    }
}