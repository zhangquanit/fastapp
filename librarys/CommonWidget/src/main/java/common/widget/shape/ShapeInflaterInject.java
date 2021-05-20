package common.widget.shape;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.lang.reflect.Field;

/**
 * 注入自定义LayoutInflater.Factory2
 * 保证自定义属性得到解析
 *
 * @author zhangquan
 */
public class ShapeInflaterInject {

    public static void inject(Context ctx) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        LayoutInflater.Factory factory = inflater.getFactory();
        LayoutInflater.Factory2 factory2 = inflater.getFactory2();
        if (null == factory2) {
            ShapeInflaterFactory shapeInflater = new ShapeInflaterFactory();
            if (ctx instanceof AppCompatActivity) {
                final AppCompatDelegate delegate = ((AppCompatActivity) ctx).getDelegate();
                shapeInflater.setImplFactory2(new LayoutInflater.Factory2() {
                    @Nullable
                    @Override
                    public View onCreateView(@Nullable View parent, String name, Context context, AttributeSet attrs) {
                        return delegate.createView(parent, name, context, attrs);
                    }

                    @Nullable
                    @Override
                    public View onCreateView(String name, Context context, AttributeSet attrs) {
                        return onCreateView(null, name, context, attrs);
                    }
                });
            }

            inflater.setFactory2(shapeInflater);
            return;
        }

        if (!(factory2 instanceof ShapeInflaterFactory)) {
            try {
                Class<? extends LayoutInflater> inflaterClass = inflater.getClass();
                ShapeInflaterFactory shapeFactory = new ShapeInflaterFactory();

                Class<?> cls = inflaterClass;
                while (cls != null && cls != Object.class) {
                    try {
                        Field mFactory = cls.getDeclaredField("mFactory");
                        mFactory.setAccessible(true);
                        Field mFactory2 = cls.getDeclaredField("mFactory2");
                        mFactory2.setAccessible(true);
                        if (factory2 != null) {
                            shapeFactory.setImplFactory2(factory2);
                        } else if (factory != null) {
                            shapeFactory.setImplFactory(factory);
                        }
                        mFactory2.set(inflater, shapeFactory);
                        mFactory.set(inflater, shapeFactory);
                        break;
                    } catch (Exception e) {
                        cls = cls.getSuperclass();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
