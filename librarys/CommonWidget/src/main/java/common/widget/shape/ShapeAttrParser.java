package common.widget.shape;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.BlendMode;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Constructor;
import java.util.Map;

import common.widget.R;


/**
 * 属性解析
 *
 * @author zhangquan
 */
public class ShapeAttrParser {
    private static final int RADIUS_TYPE_PIXELS = 0;
    private static final int RADIUS_TYPE_FRACTION = 1;
    private static final int RADIUS_TYPE_FRACTION_PARENT = 2;

    private static final float DEFAULT_INNER_RADIUS_RATIO = 3.0f;
    private static final float DEFAULT_THICKNESS_RATIO = 9.0f;

    private static final Class<?>[] sConstructorSignature = new Class[]{Context.class, AttributeSet.class};
    private static final Object[] mConstructorArgs = new Object[2];
    private static final Map<String, Constructor<? extends View>> sConstructorMap = new ArrayMap<>();

    private static final String sViewClassPrefix = "android.view.";
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit.",
            "android.app."
    };


    public static View parseAttr(Context context, AttributeSet attrs, View view) {
        return parseAttr(context, attrs, view, null);
    }

    public static View parseAttr(Context context, AttributeSet attrs, View view, String viewName) {
        if (null == attrs || Build.VERSION.SDK_INT <= 19) {
            return view;
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeBg);
        if (typedArray.getIndexCount() == 0) {
            return view;
        }
        if (null == view) {
            view = createViewFromName(context, viewName, attrs);
        }
        if (null != view) {
            try {
                GradientDrawable gradientDrawable = new GradientDrawable();
                //<shape>
                updateStateFromTypedArray(typedArray, gradientDrawable);
                //<gradient>
                updateGradientDrawableGradient(context, typedArray, gradientDrawable);
                //<solid>
                updateGradientDrawableSolid(typedArray, gradientDrawable);
                //<stroke>
                updateGradientDrawableStroke(typedArray, gradientDrawable);
                //<corners>
                updateDrawableCorners(typedArray, gradientDrawable);
                //<padding>
                updateGradientDrawablePadding(typedArray, gradientDrawable);
                //<size>
                updateGradientDrawableSize(typedArray, gradientDrawable);

                view.setBackground(gradientDrawable);
            } finally {
                typedArray.recycle();
            }
        }
        return view;
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void updateStateFromTypedArray(TypedArray a, GradientDrawable gradientDrawable) {
        int shape = a.getInt(R.styleable.ShapeBg_sp_shape, GradientDrawable.RECTANGLE);
        gradientDrawable.setShape(shape);

        boolean dither = a.getBoolean(R.styleable.ShapeBg_sp_dither, false);
        gradientDrawable.setDither(dither);

        if (shape == GradientDrawable.RING) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                int mInnerRadius = a.getDimensionPixelSize(
                        R.styleable.ShapeBg_sp_innerRadius, -1);
                gradientDrawable.setInnerRadius(mInnerRadius);
                if (mInnerRadius == -1) {
                    float mInnerRadiusRatio = a.getFloat(
                            R.styleable.ShapeBg_sp_innerRadiusRatio, DEFAULT_INNER_RADIUS_RATIO);
                    gradientDrawable.setInnerRadiusRatio(mInnerRadiusRatio);
                }

                int mThickness = a.getDimensionPixelSize(
                        R.styleable.ShapeBg_sp_thickness, -1);
                gradientDrawable.setThickness(mThickness);
                if (mThickness == -1) {
                    float mThicknessRatio = a.getFloat(
                            R.styleable.ShapeBg_sp_thicknessRatio, DEFAULT_THICKNESS_RATIO);
                    gradientDrawable.setThicknessRatio(mThicknessRatio);
                }
            }

            boolean mUseLevelForShape = a.getBoolean(
                    R.styleable.ShapeBg_sp_useLevel, true);
            gradientDrawable.setUseLevel(mUseLevelForShape);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            final int tintMode = a.getInt(R.styleable.ShapeBg_sp_tintMode, -1);
            if (tintMode != -1) {
                BlendMode blendMode = parseBlendMode(tintMode, BlendMode.SRC_IN);
                gradientDrawable.setTintBlendMode(blendMode);
            }
        }

        final ColorStateList tint = a.getColorStateList(R.styleable.ShapeBg_sp_tint);
        if (tint != null) {
            gradientDrawable.setTintList(tint);
        }

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
        if (typedArray.hasValue(R.styleable.ShapeBg_sp_width)
                || typedArray.hasValue(R.styleable.ShapeBg_sp_height)) {
            int width = typedArray.getDimensionPixelSize(
                    R.styleable.ShapeBg_sp_width, -1);
            int height = typedArray.getDimensionPixelSize(
                    R.styleable.ShapeBg_sp_height, -1);
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

        mUseLevel = typedArray.getBoolean(R.styleable.ShapeBg_sp_gradientUseLevel, mUseLevel);
        gradientDrawable.setUseLevel(mUseLevel);

        gradientType = typedArray.getInt(R.styleable.ShapeBg_sp_gradientType, gradientType);
        gradientDrawable.setGradientType(gradientType);

        mCenterX = getFloatOrFraction(typedArray, R.styleable.ShapeBg_sp_gradientCenterX, mCenterX);
        mCenterY = getFloatOrFraction(typedArray, R.styleable.ShapeBg_sp_gradientCenterY, mCenterY);

        int startColor = typedArray.getColor(R.styleable.ShapeBg_sp_gradientStartColor, 0);
        int centerColor = typedArray.getColor(R.styleable.ShapeBg_sp_gradientCenterColor, 0);
        boolean hasCenterColor = typedArray.hasValue(R.styleable.ShapeBg_sp_gradientCenterColor);
        int endColor = typedArray.getColor(R.styleable.ShapeBg_sp_gradientEndColor, 0);
        if (hasCenterColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int[] colors = new int[]{startColor, centerColor, endColor};
            float[] positions = new float[]{0.0f, mCenterX != 0.5f ? mCenterX : mCenterY, 1f};
            gradientDrawable.setColors(colors, positions);
        } else {
            int[] colors = new int[]{startColor, endColor};
            gradientDrawable.setColors(colors);
        }

        GradientDrawable.Orientation mOrientation = GradientDrawable.Orientation.TOP_BOTTOM;
        int angle = (int) typedArray.getFloat(R.styleable.ShapeBg_sp_gradientAngle, 0);
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


        final TypedValue tv = typedArray.peekValue(R.styleable.ShapeBg_sp_gradientRadius);
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void updateGradientDrawableSolid(TypedArray typedArray, GradientDrawable gradientDrawable) {
        final ColorStateList solidColor = typedArray.getColorStateList(
                R.styleable.ShapeBg_sp_solid);
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void updateGradientDrawableStroke(TypedArray typedArray, GradientDrawable gradientDrawable) {
        if (!typedArray.hasValue(R.styleable.ShapeBg_sp_strokeWidth)) {
            return;
        }

        int strokeWidth = typedArray.getDimensionPixelSize(R.styleable.ShapeBg_sp_strokeWidth, 0);
        float dashWidth = typedArray.getDimension(R.styleable.ShapeBg_sp_strokeDashWidth, 0);
        ColorStateList strokeColor = typedArray.getColorStateList(
                R.styleable.ShapeBg_sp_strokeColor);
        if (dashWidth != 0.0f) {
            float dashGap = typedArray.getDimension(R.styleable.ShapeBg_sp_strokeDashGap, 0);
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

        int radius = typedArray.getDimensionPixelSize(R.styleable.ShapeBg_sp_radius, 0);
        final int topLeftRadius = typedArray.getDimensionPixelSize(
                R.styleable.ShapeBg_sp_topLeftRadius, radius);
        final int topRightRadius = typedArray.getDimensionPixelSize(
                R.styleable.ShapeBg_sp_topRightRadius, radius);
        final int bottomLeftRadius = typedArray.getDimensionPixelSize(
                R.styleable.ShapeBg_sp_bottomLeftRadius, radius);
        final int bottomRightRadius = typedArray.getDimensionPixelSize(
                R.styleable.ShapeBg_sp_bottomRightRadius, radius);
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
     * <p>
     * 注意会覆盖View的android:padding属性
     */
    private static void updateGradientDrawablePadding(TypedArray typedArray, GradientDrawable gradientDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int padding = typedArray.getDimensionPixelOffset(R.styleable.ShapeBg_sp_padding, 0);
            if (padding > 0) {
                gradientDrawable.setPadding(padding, padding, padding, padding);
            } else {
                int paddingLeft = typedArray.getDimensionPixelOffset(R.styleable.ShapeBg_sp_paddingLeft, 0);
                int paddingTop = typedArray.getDimensionPixelOffset(R.styleable.ShapeBg_sp_paddingTop, 0);
                int paddingRight = typedArray.getDimensionPixelOffset(R.styleable.ShapeBg_sp_paddingRight, 0);
                int paddingBottom = typedArray.getDimensionPixelOffset(R.styleable.ShapeBg_sp_paddingBottom, 0);
                if (paddingLeft > 0 || paddingTop > 0 || paddingRight > 0 || paddingBottom > 0) {
                    gradientDrawable.setPadding(paddingLeft,
                            paddingTop,
                            paddingRight,
                            paddingBottom);
                }
            }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        return defaultMode;
    }


    private static View createViewFromName(Context context, String name, AttributeSet attrs) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }

        View view = null;
        try {
            mConstructorArgs[0] = context;
            mConstructorArgs[1] = attrs;

            if (!name.contains(".")) {
                if (TextUtils.equals(name, "View")) {
                    view = createView(context, name, sViewClassPrefix);
                }

                if (view == null) {
                    for (String prefix : sClassPrefixList) {
                        try {
                            view = createView(context, name, prefix);
                            if (view != null) {
                                break;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } else {
                view = createView(context, name, null);
            }
        } finally {
            mConstructorArgs[0] = null;
            mConstructorArgs[1] = null;
        }
        return view;
    }

    private static View createView(Context context, String name, String prefix) {
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        try {
            if (constructor == null) {
                Class<? extends View> clazz = Class.forName(prefix != null ? (prefix + name) : name, false,
                        context.getClassLoader()).asSubclass(View.class);
                constructor = clazz.getConstructor(sConstructorSignature);
                sConstructorMap.put(name, constructor);
            }
            constructor.setAccessible(true);
            return constructor.newInstance(mConstructorArgs);
        } catch (Exception e) {
            return null;
        }
    }
}
