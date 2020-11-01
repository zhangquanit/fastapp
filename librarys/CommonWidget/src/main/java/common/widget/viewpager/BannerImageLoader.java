package common.widget.viewpager;

import android.content.Context;

/**
 * @author 张全
 */
public interface BannerImageLoader<V, T> {
    void displayView(Context ctx, T data, V view, int pos, int count);

    V createView(Context ctx);
}
