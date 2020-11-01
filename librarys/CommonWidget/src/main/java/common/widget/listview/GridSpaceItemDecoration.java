package common.widget.listview;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int headerCount;
    private int horizontalSpace;
    private int verticalSpace;
    private int spanCount = 2;


    public GridSpaceItemDecoration(int space) {
        this(space, space);
    }

    public GridSpaceItemDecoration(int horizontalSpace, int verticalSpace) {
        this.horizontalSpace = horizontalSpace;
        this.verticalSpace = verticalSpace;
    }

    public void setHeaderCount(int headerCount) {
        this.headerCount = headerCount;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //不是第一个的格子都设一个左边和底部的间距
        outRect.left = horizontalSpace;
        outRect.bottom = verticalSpace;
        //由于每行都只有2个，所以第一个都是2的倍数，把左边距设为0
        if ((parent.getChildLayoutPosition(view)-headerCount) % spanCount == 0) {
            outRect.left = 0;
        }
    }
}