package common.widget.shape;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;


import common.widget.R;

/**
 * @author zhangquan
 */
public class ShapeHelper {

    public static GradientDrawable parseAttr(Context context, AttributeSet attrs) {
        if (attrs != null) {  // 获取自定义属性
            GradientDrawable gradientDrawable = new GradientDrawable();
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeLayout);

            //半径
            int radius = (int) typedArray.getDimension(R.styleable.ShapeLayout_radius, 0);
            final int topLeftRadius = typedArray.getDimensionPixelSize(
                    R.styleable.ShapeLayout_topLeftRadius, radius);
            final int topRightRadius = typedArray.getDimensionPixelSize(
                    R.styleable.ShapeLayout_topRightRadius, radius);
            final int bottomLeftRadius = typedArray.getDimensionPixelSize(
                    R.styleable.ShapeLayout_bottomLeftRadius, radius);
            final int bottomRightRadius = typedArray.getDimensionPixelSize(
                    R.styleable.ShapeLayout_bottomRightRadius, radius);
            if (topLeftRadius != radius || topRightRadius != radius ||
                    bottomLeftRadius != radius || bottomRightRadius != radius) {
                gradientDrawable.setCornerRadii(new float[]{
                        topLeftRadius, topLeftRadius,
                        topRightRadius, topRightRadius,
                        bottomRightRadius, bottomRightRadius,
                        bottomLeftRadius, bottomLeftRadius
                });
            } else {
                gradientDrawable.setCornerRadius(radius);
            }

            //背景色
            final ColorStateList solidColor = typedArray.getColorStateList(
                    R.styleable.ShapeLayout_solid);
            if (solidColor != null) {
                gradientDrawable.setColor(solidColor);
            }

            //边框
            int strokeWidth = typedArray.getDimensionPixelSize(R.styleable.ShapeLayout_strokeWidth, 0);
            int dashWidth = typedArray.getDimensionPixelSize(R.styleable.ShapeLayout_strokeDashWidth, 0);
            ColorStateList strokeColor = typedArray.getColorStateList(
                    R.styleable.ShapeLayout_strokeColor);
            if (dashWidth != 0) {
                int dashGap = typedArray.getDimensionPixelSize(R.styleable.ShapeLayout_strokeDashGap, 0);
                gradientDrawable.setStroke(strokeWidth, strokeColor, dashWidth, dashGap);
            } else {
                gradientDrawable.setStroke(strokeWidth, strokeColor);
            }

            //padding
            int padding = typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_padding, 0);
            if (padding > 0) {
                gradientDrawable.setPadding(padding, padding, padding, padding);
            } else {
                gradientDrawable.setPadding(typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_paddingLeft, 0),
                        typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_paddingTop, 0),
                        typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_paddingRight, 0),
                        typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_paddingBottom, 0));
            }
            typedArray.recycle();

            return gradientDrawable;
        }
        return null;
    }
}
