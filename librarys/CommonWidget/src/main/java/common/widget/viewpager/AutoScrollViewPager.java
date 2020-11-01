package common.widget.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.view.MotionEventCompat;


public class AutoScrollViewPager extends common.widget.viewpager.ViewPager {
    private float touchX = 0f, downX = 0f;
    // 滑动距离及坐标
    private float xDistance, yDistance, xLast, yLast;
    private BannerViewPager mBannerViewPager;

    public AutoScrollViewPager(Context context) {
        super(context);
    }

    public AutoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBanner(BannerViewPager banner) {
        mBannerViewPager = banner;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        touchX = ev.getX();

        if ((action == MotionEvent.ACTION_DOWN)) {
            // 暂停滚动
            downX = touchX;
            mBannerViewPager.pauseAutoScroll();
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            // 继续滚动
            mBannerViewPager.resumeAutoScroll();
        }

        // -----------是否拦截手势
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;

                if (xDistance > yDistance) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
        }
        return super.dispatchTouchEvent(ev);
    }
}
