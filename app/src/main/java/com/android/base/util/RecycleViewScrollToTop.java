package com.android.base.util;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * @author 张全
 */
public class RecycleViewScrollToTop {


    public static void addScroolToTop(RecyclerView recyclerView, ImageView ivTop, StaggeredGridLayoutManager layoutManager) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //获得recyclerView的线性布局管理器
                //获取到第一个item的显示的下标  不等于0表示第一个item处于不可见状态 说明列表没有滑动到顶部 显示回到顶部按钮
                int[] pos = new int[10];
                layoutManager.findFirstVisibleItemPositions(pos);
                // 当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 判断是否滚动超过一屏
                    if (pos[0] == 0) {
                        ivTop.setVisibility(View.GONE);
                    } else {
                        //显示回到顶部按钮
                        ivTop.setVisibility(View.VISIBLE);
                    }
                    //获取RecyclerView滑动时候的状态
                }
                else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {//拖动中
                    ivTop.setVisibility(View.GONE);
                }
            }
        });
        ivTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(0);
                ivTop.setVisibility(View.GONE);
            }
        });
    }


    public static void addScroolToTop(RecyclerView recyclerView, ImageView ivTop) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //获得recyclerView的线性布局管理器
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //获取到第一个item的显示的下标  不等于0表示第一个item处于不可见状态 说明列表没有滑动到顶部 显示回到顶部按钮
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
                // 当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 判断是否滚动超过一屏
                    if (firstVisibleItemPosition == 0) {
                        ivTop.setVisibility(View.GONE);
                    } else {
                        //显示回到顶部按钮
                        ivTop.setVisibility(View.VISIBLE);
                    }
                    //获取RecyclerView滑动时候的状态
                }
            }
        });
        ivTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(0);
//                recyclerView.smoothScrollToPosition(0);
                ivTop.setVisibility(View.GONE);
            }
        });
    }
}
