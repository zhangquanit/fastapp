package common.widget.shape;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import common.widget.R;
import common.widget.shape.view.ShapeLinearLayout;

/**
 * @author zhangquan
 */
public class ShapeInflaterFactory implements LayoutInflater.Factory2 {
    private LayoutInflater.Factory2 mFactory2;
    private LayoutInflater.Factory mFactory;
    private static String sFilterPkg;

    static {
        String name = ShapeLinearLayout.class.getName();
        sFilterPkg = name.substring(0, name.lastIndexOf("."));
    }

    //factory2
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (name.startsWith(sFilterPkg)) {
            return null;
        }
//        int n = attrs.getAttributeCount();
//        for (int i = 0; i < n; i++) {
//            System.out.println(attrs.getAttributeName(i) + "=" + attrs.getAttributeValue(i));
//        }

        View view = null;
        if (null != mFactory2) {
            view = mFactory2.onCreateView(parent, name, context, attrs);
        } else if (mFactory != null) {
            view = mFactory.onCreateView(name, context, attrs);
        }

        view = ShapeAttrParser.parseAttr(context, attrs, view, name);
        return view;
    }

    //factory
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    public void setImplFactory2(LayoutInflater.Factory2 factory2) {
        this.mFactory2 = factory2;
    }

    public void setImplFactory(LayoutInflater.Factory factory) {
        this.mFactory = factory;
    }
}
