package common.widget.viewpager;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

import common.widget.R;

/**
 * 一、功能
 * <ol>
 * <li>支持无限滚动，调用startAutoScroll开始滚动</li>
 * <li>支持无限左右滑动切换页面</li>
 * </ol>
 * 二、使用
 *
 * <pre>
 * BannerViewPager mViewPager = (BannerViewPager) findViewById(R.id.viewPager);
 *  mViewPager.setImageLoader(loader);
 *  mViewPager.setmOnItemClickListener(new AdapterView.OnItemClickListener() {
 *             @Override
 *             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 *             }
 *         });
 * mViewPager.setInterval(3 * 1000);// 设置滚动间隔时长
 * mViewPager.setScrollDurationFactor(4.0f);// 设置自动滚动的Scroller时长因子
 * mViewPager.startAutoScroll(4 * 1000);// 延迟4秒开始自动滚动
 * </pre>
 * <p>
 * 三、注意
 * <ol>
 * <li>为避免内存浪费，请在Activity或Fragment的onRause中暂停滚动，onResume中继续滚动</li>
 * </ol>
 *
 * @author zhangquan
 */
public class BannerViewPager<T> extends RelativeLayout {
    public String tag = BannerViewPager.class.getSimpleName();
    private AutoScrollViewPager viewPager;
    private LinearLayout indicator;
    private int mIndicatorSize, mIndicatorMargin;
    private BannerPagerAdapter adapter;
    private List<T> dataList;
    private List<View> views = new ArrayList<>();
    private List<ImageView> indicatorImages = new ArrayList<>();
    private int count = 0;
    private int currentItem = 1;
    private int lastPosition;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private BannerImageLoader imageLoader;

    public static final int DEFAULT_INTERVAL = 1500;
    private boolean started;
    private TimerHandler timerHandler;
    private CustomDurationScroller mScroller = null;
    private long interval = DEFAULT_INTERVAL;
    private int imageHeight;


    public BannerViewPager(Context context) {
        super(context);
        init();
    }

    public BannerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BannerViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.banner_layout, this);
        viewPager = findViewById(R.id.bannerViewPager);
        viewPager.setBanner(this);
        mScroller = viewPager.mScroller;
        indicator = findViewById(R.id.circleIndicator);
        mIndicatorSize = dip2px(getContext(), 5);
        mIndicatorMargin = dip2px(getContext(), 5);

        timerHandler = new TimerHandler();

    }

    public static int dip2px(Context context, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                context.getResources().getDisplayMetrics());
    }


    public void setDataList(List<T> list) {
        dataList = list;
        count = dataList.size();

        //指示器
        createIndicator();
        //banner
        setImageList();
        setData();
    }

    private void setImageList() {
        if (dataList == null || dataList.size() <= 0) {
            return;
        }
        if (imageLoader == null) {
            throw new RuntimeException("imageLoader不能为null");
        }
        viewPager.setPagingEnabled(count > 1);
        views.clear();
        if (count == 1) { //只有一条数据
            createItemView(0);
        } else {
            for (int i = 0; i <= count + 1; i++) {
                createItemView(i);
            }
        }
    }

    private void createItemView(int index) {
        View view = null;
        if (imageLoader != null) {
            view = (View) imageLoader.createView(getContext());
        }
        if (view == null) {
            view = new ImageView(getContext());
        }

        T t;
        if (index == 0) {
            t = dataList.get(count - 1);
            imageLoader.displayView(getContext(), t, view, count - 1, count);
        } else if (index == count + 1) {
            t = dataList.get(0);
            imageLoader.displayView(getContext(), t, view, 0, count);
        } else {
            t = dataList.get(index - 1);
            imageLoader.displayView(getContext(), t, view, index - 1, count);
        }
        views.add(view);
    }

    private void createIndicator() {
        int visibility = count > 1 ? View.VISIBLE : View.GONE;
        indicator.setVisibility(visibility);
        indicatorImages.clear();
        indicator.removeAllViews();
        if (count <= 1) {
            return;
        }
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorSize, mIndicatorSize);
            params.leftMargin = mIndicatorMargin;
            params.rightMargin = mIndicatorMargin;
            imageView.setImageResource(R.drawable.indicator_selector);
            if (i == 0) {
                imageView.setSelected(true);
            }
            indicatorImages.add(imageView);
            indicator.addView(imageView, params);
        }
    }


    private void setData() {
        currentItem = 1;
//        if (adapter == null) {
        adapter = new BannerPagerAdapter();
        viewPager.addOnPageChangeListener(pageChangeListener);
//        }
        viewPager.setAdapter(adapter);
        viewPager.setFocusable(true);
        viewPager.setCurrentItem(1);

    }

    public List<T> getDataList() {
        return dataList;
    }


    /**
     * 返回真实的位置
     *
     * @param position
     * @return 下标从0开始
     */
    public int toRealPosition(int position) {
        if (count == 0) return 0;
        int realPosition = (position - 1) % count;
        if (realPosition < 0)
            realPosition += count;
        return realPosition;
    }

    class BannerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = views.get(position);
            container.addView(itemView);
            View view = itemView;
            if (mOnItemClickListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(null, v, toRealPosition(position), 1);
                    }
                });
            }
//            int height = itemView.getHeight();
//            if (height != imageHeight) {
//                itemView.setMinimumHeight(imageHeight);
//            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int state) {
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageScrollStateChanged(state);
            }
            switch (state) {
                case 0://No operation
                    if (currentItem == 0) {
                        viewPager.setCurrentItem(count, false);
                    } else if (currentItem == count + 1) {
                        viewPager.setCurrentItem(1, false);
                    }
                    break;
                case 1://start Sliding
                    if (currentItem == count + 1) {
                        viewPager.setCurrentItem(1, false);
                    } else if (currentItem == 0) {
                        viewPager.setCurrentItem(count, false);
                    }
                    break;
                case 2://end Sliding
                    break;
            }
        }


        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageScrolled(toRealPosition(position), positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            currentItem = position;
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageSelected(toRealPosition(position));
            }

            if (indicatorImages.isEmpty()) {
                return;
            }
            indicatorImages.get((lastPosition - 1 + count) % count).setSelected(false);
            indicatorImages.get((position - 1 + count) % count).setSelected(true);
            lastPosition = position;
        }
    };
//------------------------------------------

    /**
     * 开始自动滚动
     */
    public void startAutoScroll() {
        startAutoScroll(0);
    }

    /**
     * 延迟delayTimeInMills秒后开始自动滚动
     *
     * @param delayTime
     */
    public void startAutoScroll(long delayTime) {
        started = true;
        timerHandler.sendScrollMsg(delayTime);
    }

    /**
     * 停止滚动
     */
    public void stopAutoScroll() {
        started = false;
        timerHandler.pause();
    }

    /**
     * 暂停滚动
     */
    public void pauseAutoScroll() {
        if (isStarted()) {
            timerHandler.pause();
        }
        if (null != mScroller) mScroller.resetDurationFactor();
    }

    /**
     * 继续滚动
     */
    public void resumeAutoScroll() {
        if (isStarted()) {
            timerHandler.resume();
        }
    }

    /**
     * 是否已开始滚动
     *
     * @return
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * 两次滚动之间的时间间隔
     *
     * @return
     */
    public long getInterval() {
        return interval;
    }

    /**
     * 设置两次滚动之间的时间间隔
     *
     * @param interval
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * 定时器
     *
     * @author zhangquan
     */
    @SuppressLint("HandlerLeak")
    private class TimerHandler extends Handler {
        private final int msg_start = 1;
        private boolean paused;// 是否处于暂停

        public TimerHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (paused) {
                return;
            }

            if (count > 1) {
                if (null != mScroller) {
                    mScroller.setScrollDurationFactor(mScroller.getScrollDuraionFactor());
                }
                currentItem = currentItem % (count + 1) + 1;
                if (currentItem == 1) {
                    viewPager.setCurrentItem(currentItem, false);
                    sendScrollMsg(0);
                } else {
                    viewPager.setCurrentItem(currentItem);
                    sendScrollMsg(interval + 500);
                }
            }
        }

        /**
         * 继续
         */
        public synchronized void resume() {
            paused = false;
            sendScrollMsg(interval);
        }

        /**
         * 暂停
         */
        public synchronized void pause() {
            paused = true;
            if (hasMessages(msg_start)) {
                removeMessages(msg_start);
            }
        }

        public void sendScrollMsg(long delayTime) {
            if (hasMessages(msg_start)) {
                removeMessages(msg_start);
            }
            sendMessageDelayed(obtainMessage(msg_start), delayTime);
        }
    }


    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    public void setmOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setImageLoader(BannerImageLoader loader) {
        imageLoader = loader;
    }

//    public void setImageHeight(int height) {
//        this.imageHeight = height;
//        for (View view : views) {
//            view.setMinimumHeight(imageHeight);
//        }
//    }

    /**
     * ---------------------- ViewPager支持 api
     */
    public void setPageMargin(int marginPixels) {
        viewPager.setPageMargin(marginPixels);
    }

    public void setPageTransformer(boolean reverseDrawingOrder, @Nullable ViewPager.PageTransformer transformer) {
        viewPager.setPageTransformer(reverseDrawingOrder, transformer);
    }

    public void setOffscreenPageLimit(int limit) {
        viewPager.setOffscreenPageLimit(limit);
    }

    public void setScrollDurationFactor(float scrollFactor) {
        viewPager.setScrollDurationFactor(scrollFactor);
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public PagerAdapter getAdapter() {
        return viewPager.getAdapter();
    }

    public int getCurrentItem() {
        return toRealPosition(currentItem);
    }


}
