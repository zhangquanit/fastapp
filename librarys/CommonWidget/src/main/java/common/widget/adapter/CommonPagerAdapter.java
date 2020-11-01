package common.widget.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ViewPager通用的Adapter
 *
 * @param <E>
 * @author jiahongya
 */
public abstract class CommonPagerAdapter<E> extends PagerAdapter {
    private List<E> mList = Collections.synchronizedList(new ArrayList<E>());
    protected SparseArray<View> views = new SparseArray<>();
    protected Context mContext;
    private int mLayoutId;

    public CommonPagerAdapter(Context context, List<E> list, int layoutId) {
        mContext = context;
        this.mLayoutId = layoutId;
        if (null != list) {
            mList.addAll(list);
            list = mList;
        }
        resetViews();
    }

    public void refresh(List<E> list) {
        this.mList = list;
        resetViews();
        notifyDataSetChanged();
    }

    protected void resetViews() {
        views.clear();
        int dataSize = getList().size();
        int viewCount = dataSize > 1 ? dataSize * 2 : dataSize;
        for (int i = 0; i < viewCount; i++) {
            views.put(i, View.inflate(mContext, mLayoutId, null));
        }
    }

    public Context getContext() {
        return mContext;
    }

    public E getItem(int pPosition) {
        return mList.get(pPosition);
    }

    public List<E> getList() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int viewIndex = position % views.size();

        View childView = views.get(viewIndex);

        if (null == childView) {
            for (int i = 0; i < views.size(); i++) {
                View view = views.valueAt(i);
                if (null == view.getParent()) {
                    childView = view;
                    break;
                }
            }
//			for (Map.Entry<Integer, View> entry : views.entrySet()) {
//				childView = entry.getValue();
//				if (null == childView.getParent()) {
//					break;
//				}
//			}
        }
        if (null == childView) {
            childView = View.inflate(mContext, mLayoutId, null);
            views.put(viewIndex, childView);
        }

        try {
            container.addView(childView);
        } catch (Exception e) {
            e.printStackTrace();
            childView = View.inflate(mContext, mLayoutId, null);
            views.put(viewIndex, childView);
            container.addView(childView);
        }

        int dataIndex = position % getList().size();
        E data = getList().get(dataIndex);
        setItemData(childView, dataIndex, data);
        return childView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    /**
     * 给item设置数据
     *
     * @param contentView 当前页view
     * @param position    当前页面下标
     * @param data        给当前页设置的数据
     */
    public abstract void setItemData(View contentView, int position, E data);

}
