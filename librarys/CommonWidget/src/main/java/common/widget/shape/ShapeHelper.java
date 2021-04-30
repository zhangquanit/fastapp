package common.widget.shape;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;

import common.widget.R;

/**
 * @author zhangquan
 */
public class ShapeHelper {
    private static final int RADIUS_TYPE_PIXELS = 0;
    private static final int RADIUS_TYPE_FRACTION = 1;
    private static final int RADIUS_TYPE_FRACTION_PARENT = 2;

    private static final float DEFAULT_INNER_RADIUS_RATIO = 3.0f;
    private static final float DEFAULT_THICKNESS_RATIO = 9.0f;

    public static GradientDrawable parseAttr(Context context, AttributeSet attrs) {
        if (null == attrs || Build.VERSION.SDK_INT <= 19) {
            return null;
        }

        GradientDrawable gradientDrawable = new GradientDrawable();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeLayout);

        updateStateFromTypedArray(typedArray, gradientDrawable);
        updateGradientDrawableSize(typedArray, gradientDrawable);
        updateGradientDrawableGradient(context, typedArray, gradientDrawable);
        updateGradientDrawableSolid(typedArray, gradientDrawable);
        updateGradientDrawableStroke(typedArray, gradientDrawable);
        updateDrawableCorners(typedArray, gradientDrawable);
        updateGradientDrawablePadding(typedArray, gradientDrawable);

        typedArray.recycle();

        return gradientDrawable;
    }


    /**
     * <shape xmlns:android="http://schemas.android.com/apk/res/android"
     * android:shape="rectangle"
     * android:useLevel="false"
     * android:dither="true"
     * android:innerRadius="10dp"
     * android:innerRadiusRatio="1.2"
     * android:tint="#fff"
     * android:tintMode="src_in"
     * android:visible="true"
     * android:thickness="10dp"
     * android:thicknessRatio="1.2"
     * >
     */
    private static void updateStateFromTypedArray(TypedArray a, GradientDrawable gradientDrawable) {
        int shape = a.getInt(R.styleable.ShapeLayout_shape, GradientDrawable.RECTANGLE);
        gradientDrawable.setShape(shape);

        boolean dither = a.getBoolean(R.styleable.ShapeLayout_dither, false);
        gradientDrawable.setDither(dither);

        if (shape == GradientDrawable.RING) {
            int mInnerRadius = a.getDimensionPixelSize(
                    R.styleable.ShapeLayout_innerRadius, -1);
            gradientDrawable.setInnerRadius(mInnerRadius);
            if (mInnerRadius == -1) {
                float mInnerRadiusRatio = a.getFloat(
                        R.styleable.ShapeLayout_mInnerRadiusRatio, DEFAULT_INNER_RADIUS_RATIO);
                gradientDrawable.setInnerRadiusRatio(mInnerRadiusRatio);
            }

            int mThickness = a.getDimensionPixelSize(
                    R.styleable.ShapeLayout_thickness, -1);
            gradientDrawable.setThickness(mThickness);
            if (mThickness == -1) {
                float mThicknessRatio = a.getFloat(
                        R.styleable.ShapeLayout_thicknessRatio, DEFAULT_THICKNESS_RATIO);
                gradientDrawable.setThicknessRatio(mThicknessRatio);
            }

            boolean mUseLevelForShape = a.getBoolean(
                    R.styleable.ShapeLayout_useLevel, true);
            gradientDrawable.setUseLevel(mUseLevelForShape);
        }

//        final int tintMode = a.getInt(R.styleable.ShapeLayout_tintMode, -1);
//        if (tintMode != -1) {
//            BlendMode blendMode = parseBlendMode(tintMode, BlendMode.SRC_IN);
//            gradientDrawable.setTintBlendMode(blendMode);
//        }
//
//        final ColorStateList tint = a.getColorStateList(R.styleable.ShapeLayout_tint);
//        if (tint != null) {
//            gradientDrawable.setTintList(tint);
//        }
//        Insets mOpticalInsets = Insets.NONE;
//        final int insetLeft = a.getDimensionPixelSize(
//                R.styleable.ShapeLayout_opticalInsetLeft, mOpticalInsets.left);
//        final int insetTop = a.getDimensionPixelSize(
//                R.styleable.ShapeLayout_opticalInsetTop, mOpticalInsets.top);
//        final int insetRight = a.getDimensionPixelSize(
//                R.styleable.ShapeLayout_opticalInsetRight, mOpticalInsets.right);
//        final int insetBottom = a.getDimensionPixelSize(
//                R.styleable.ShapeLayout_opticalInsetBottom, mOpticalInsets.bottom);
//        mOpticalInsets = Insets.of(insetLeft, insetTop, insetRight, insetBottom);

    }

    /**
     * <size android:width="" android:height=""/>
     */
    private static void updateGradientDrawableSize(TypedArray typedArray, GradientDrawable gradientDrawable) {
        if (typedArray.hasValue(R.styleable.ShapeLayout_width)
                || typedArray.hasValue(R.styleable.ShapeLayout_height)) {
            int width = typedArray.getDimensionPixelSize(
                    R.styleable.ShapeLayout_width, -1);
            int height = typedArray.getDimensionPixelSize(
                    R.styleable.ShapeLayout_height, -1);
            gradientDrawable.setSize(width, height);
        }
    }

    /**
     * <gradient
     * android:angle=""
     * android:centerColor=""
     * android:centerX=""
     * android:centerY=""
     * android:endColor=""
     * android:startColor=""
     * android:type=""
     * android:gradientRadius=""
     * android:useLevel=""
     * />
     */
    private static void updateGradientDrawableGradient(Context context, TypedArray typedArray, GradientDrawable gradientDrawable) {

        float mCenterX = 0.5f;
        float mCenterY = 0.5f;
        float mGradientRadius = 0.5f;
        int mGradientRadiusType = RADIUS_TYPE_PIXELS;
        boolean mUseLevel = false;
        int gradientType = GradientDrawable.LINEAR_GRADIENT;

        mUseLevel = typedArray.getBoolean(R.styleable.ShapeLayout_gradientUseLevel, mUseLevel);
        gradientDrawable.setUseLevel(mUseLevel);

        gradientType = typedArray.getInt(R.styleable.ShapeLayout_gradientType, gradientType);
        gradientDrawable.setGradientType(gradientType);

        mCenterX = getFloatOrFraction(typedArray, R.styleable.ShapeLayout_gradientCenterX, mCenterX);
        mCenterY = getFloatOrFraction(typedArray, R.styleable.ShapeLayout_gradientCenterY, mCenterY);

        int startColor = typedArray.getColor(R.styleable.ShapeLayout_gradientStartColor, 0);
        int centerColor = typedArray.getColor(R.styleable.ShapeLayout_gradientCenterColor, 0);
        boolean hasCenterColor = typedArray.hasValue(R.styleable.ShapeLayout_gradientCenterColor);
        int endColor = typedArray.getColor(R.styleable.ShapeLayout_gradientEndColor, 0);
        if (hasCenterColor) {
            int[] colors = new int[]{startColor, centerColor, endColor};
            float[] positions = new float[]{0.0f, mCenterX != 0.5f ? mCenterX : mCenterY, 1f};
            gradientDrawable.setColors(colors, positions);
        } else {
            int[] colors = new int[]{startColor, endColor};
            gradientDrawable.setColors(colors);
        }

        GradientDrawable.Orientation mOrientation = GradientDrawable.Orientation.TOP_BOTTOM;
        int angle = (int) typedArray.getFloat(R.styleable.ShapeLayout_gradientAngle, 0);
        angle = ((angle % 360) + 360) % 360;
        if (angle >= 0) {
            switch (angle) {
                case 0:
                    mOrientation = GradientDrawable.Orientation.LEFT_RIGHT;
                    break;
                case 45:
                    mOrientation = GradientDrawable.Orientation.BL_TR;
                    break;
                case 90:
                    mOrientation = GradientDrawable.Orientation.BOTTOM_TOP;
                    break;
                case 135:
                    mOrientation = GradientDrawable.Orientation.BR_TL;
                    break;
                case 180:
                    mOrientation = GradientDrawable.Orientation.RIGHT_LEFT;
                    break;
                case 225:
                    mOrientation = GradientDrawable.Orientation.TR_BL;
                    break;
                case 270:
                    mOrientation = GradientDrawable.Orientation.TOP_BOTTOM;
                    break;
                case 315:
                    mOrientation = GradientDrawable.Orientation.TL_BR;
                    break;
            }
        } else {
            mOrientation = GradientDrawable.Orientation.TOP_BOTTOM;
        }
        gradientDrawable.setOrientation(mOrientation);


        final TypedValue tv = typedArray.peekValue(R.styleable.ShapeLayout_gradientRadius);
        if (tv != null) {
            if (tv.type == TypedValue.TYPE_FRACTION) {
                mGradientRadius = tv.getFraction(1.0f, 1.0f);

                final int unit = (tv.data >> TypedValue.COMPLEX_UNIT_SHIFT)
                        & TypedValue.COMPLEX_UNIT_MASK;
                if (unit == TypedValue.COMPLEX_UNIT_FRACTION_PARENT) {
                    mGradientRadiusType = RADIUS_TYPE_FRACTION_PARENT;
                } else {
                    mGradientRadiusType = RADIUS_TYPE_FRACTION;
                }
            } else if (tv.type == TypedValue.TYPE_DIMENSION) {
                mGradientRadius = tv.getDimension(context.getResources().getDisplayMetrics());
                mGradientRadiusType = RADIUS_TYPE_PIXELS;
            } else {
                mGradientRadius = tv.getFloat();
                mGradientRadiusType = RADIUS_TYPE_PIXELS;
            }
            gradientDrawable.setGradientType(mGradientRadiusType);
            gradientDrawable.setGradientRadius(mGradientRadius);
        }

    }

    /**
     * <solid android:color="" />
     */
    private static void updateGradientDrawableSolid(TypedArray typedArray, GradientDrawable gradientDrawable) {
        final ColorStateList solidColor = typedArray.getColorStateList(
                R.styleable.ShapeLayout_solid);
        if (solidColor != null) {
            gradientDrawable.setColor(solidColor);
        }

    }

    /**
     * <stroke
     * android:width=""
     * android:color=""
     * android:dashWidth=""
     * android:dashGap="" />
     */
    private static void updateGradientDrawableStroke(TypedArray typedArray, GradientDrawable gradientDrawable) {

        int strokeWidth = typedArray.getDimensionPixelSize(R.styleable.ShapeLayout_strokeWidth, 0);
        float dashWidth = typedArray.getDimension(R.styleable.ShapeLayout_strokeDashWidth, 0);
        ColorStateList strokeColor = typedArray.getColorStateList(
                R.styleable.ShapeLayout_strokeColor);
        if (dashWidth != 0.0f) {
            float dashGap = typedArray.getDimension(R.styleable.ShapeLayout_strokeDashGap, 0);
            gradientDrawable.setStroke(strokeWidth, strokeColor, dashWidth, dashGap);
        } else {
            gradientDrawable.setStroke(strokeWidth, strokeColor);
        }
    }

    /**
     * <corners
     * android:bottomLeftRadius=""
     * android:bottomRightRadius=""
     * android:radius=""
     * android:topLeftRadius=""
     * android:topRightRadius="" />
     */
    private static void updateDrawableCorners(TypedArray typedArray, GradientDrawable gradientDrawable) {

        int radius = typedArray.getDimensionPixelSize(R.styleable.ShapeLayout_radius, 0);
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
    }

    /**
     * <padding
     * android:bottom=""
     * android:left=""
     * android:right=""
     * android:top="" />
     */
    private static void updateGradientDrawablePadding(TypedArray typedArray, GradientDrawable gradientDrawable) {

        int padding = typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_padding, 0);
        if (padding > 0) {
            gradientDrawable.setPadding(padding, padding, padding, padding);
        } else {
            gradientDrawable.setPadding(typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_paddingLeft, 0),
                    typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_paddingTop, 0),
                    typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_paddingRight, 0),
                    typedArray.getDimensionPixelOffset(R.styleable.ShapeLayout_paddingBottom, 0));
        }

    }

    private static float getFloatOrFraction(TypedArray a, int index, float defaultValue) {
        TypedValue tv = a.peekValue(index);
        float v = defaultValue;
        if (tv != null) {
            boolean vIsFraction = tv.type == TypedValue.TYPE_FRACTION;
            v = vIsFraction ? tv.getFraction(1.0f, 1.0f) : tv.getFloat();
        }
        return v;
    }

    public static BlendMode parseBlendMode(int value, BlendMode defaultMode) {
        switch (value) {
            case 3:
                return BlendMode.SRC_OVER;
            case 5:
                return BlendMode.SRC_IN;
            case 9:
                return BlendMode.SRC_ATOP;
            // b/73224934 PorterDuff Multiply maps to Skia Modulate so actually
            // return BlendMode.MODULATE here
            case 14:
                return BlendMode.MODULATE;
            case 15:
                return BlendMode.SCREEN;
            case 16:
                return BlendMode.PLUS;
            default:
                return defaultMode;
        }
    }
}
