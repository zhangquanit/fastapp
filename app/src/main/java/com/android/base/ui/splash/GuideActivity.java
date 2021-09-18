package com.android.base.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.base.ui.BaseActivity;
import com.fastapp.MainActivity;
import com.fastapp.R;

import net.medlinker.android.splash.SplashUtil;

import java.util.ArrayList;

/**
 * 引导页
 */
public class GuideActivity extends BaseActivity {
    private static final int[] imgs = new int[]{R.drawable.guide01, R.drawable.guide02, R.drawable.guide03};
    private ViewPager viewPager;


    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, GuideActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.guide_layout;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        ArrayList<View> viewList = new ArrayList<>();
        for (int i = 0; i < imgs.length; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.guide_item, null);
            ImageView imageView = view.findViewById(R.id.guide_img);
            imageView.setImageResource(imgs[i]);
            viewList.add(view);
            if (i == imgs.length - 1) {
                View btn = view.findViewById(R.id.tv_go_login);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SplashUtil.setShowGuide(true);
                        MainActivity.start(GuideActivity.this);
                        finish();
                    }
                });
            }
        }
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(viewList.get(position), ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                return viewList.get(position);
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        });
    }

}
