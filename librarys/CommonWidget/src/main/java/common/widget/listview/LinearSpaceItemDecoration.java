package common.widget.listview;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class LinearSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int headerCount;
    private int mOrientation;
    private int space;

    public LinearSpaceItemDecoration(int space,int oriention) {
        this.space = space;
        setOrientation(oriention);
    }

    public void setOrientation(int orientation) {
        if (orientation != 0 && orientation != 1) {
            throw new IllegalArgumentException("Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        } else {
            this.mOrientation = orientation;
        }
    }

    public void setHeaderCount(int headerCount) {
        this.headerCount = headerCount;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, 0);
        if ((parent.getChildLayoutPosition(view)-headerCount)>=0) {
            if (this.mOrientation == 1) {
                outRect.set(0, 0, 0, space);
            } else {
                outRect.set(0, 0, space, 0);
            }
        }
    }
}