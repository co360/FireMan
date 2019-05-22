// Copyright 2019 The dtxfdj. All rights reserved.
// Author: baidaogui.

package com.dtxfdj.fireman.startpage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.dtxfdj.fireman.R;
import com.dtxfdj.fireman.utils.CommonUtils;

/**
 * SlideShowView 实现的轮播图广告自定义视图
 * 既支持自动轮播页面也支持手势滑动切换页面
 * @author baidaogui
 *
 */

public class SlideShowView extends FrameLayout {

    // num of sliding image
    private final static int IMAGE_COUNT = 5;
    // time interval for auto slide
    private final static int TIME_INTERVAL = 5;
    // flag for enable auto play image
    private final static boolean isAutoPlay = false;

    // image res ID
    private int[] mImagesResIds;
    private List<ImageView> mImageViewsList;
    private List<View> mDotViewsList;

    private ViewPager mViewPager;
    // current selected page
    private int mCurrentItem  = 0;

    private ScheduledExecutorService mScheduledExecutorService;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mViewPager.setCurrentItem(mCurrentItem);
        }
    };

    public SlideShowView(Context context) {
        this(context, null);
    }

    public SlideShowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideShowView(Context context,
            AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void show() {
        initData();
        initUI(getContext());
        setVisibility(VISIBLE);
        if (isAutoPlay) {
            startPlay();
        }
    }

    private void startPlay() {
        mScheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(new SlideShowTask(), 
                1, 4, TimeUnit.SECONDS);
    }

    private void stopPlay() {
        mScheduledExecutorService.shutdown();
    }

    private void initData() {
        mImagesResIds = new int[]{
            R.drawable.start_img_1,
            R.drawable.start_img_2,
            R.drawable.start_img_3,
            R.drawable.start_img_4,
            R.drawable.start_img_5,
        };
        mImageViewsList = new ArrayList<ImageView>();
        mDotViewsList = new ArrayList<View>();
    }

    private void initUI(Context context) {
        LayoutInflater.from(context).inflate(
                R.layout.layout_slideshow, this, true);
        // subsample picture under android 5.0, because of out of memory
        int factor = Build.VERSION.SDK_INT < 21 ? 4 : 1;
        for (int imageID : mImagesResIds) {
            ImageView view =  new ImageView(context);
            view.setImageBitmap(CommonUtils.decodeSampledBitmapFromResource(
                    getResources(), imageID,
                    context.getResources().getDisplayMetrics().widthPixels / factor,
                    context.getResources().getDisplayMetrics().heightPixels / factor));
            view.setScaleType(ScaleType.FIT_XY);
            mImageViewsList.add(view);
        }
        mDotViewsList.add(findViewById(R.id.v_dot1));
        mDotViewsList.add(findViewById(R.id.v_dot2));
        mDotViewsList.add(findViewById(R.id.v_dot3));
        mDotViewsList.add(findViewById(R.id.v_dot4));
        mDotViewsList.add(findViewById(R.id.v_dot5));

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setFocusable(true);

        mViewPager.setAdapter(new MyPagerAdapter());
        mViewPager.setOnPageChangeListener(new MyPageChangeListener());
    }

    private class MyPagerAdapter  extends PagerAdapter {
        @Override
        public void destroyItem(View container,
                int position, Object object) {
            ((ViewPager) container).removeView(
                mImageViewsList.get(position));
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewPager)container).addView(mImageViewsList.get(position));
            return mImageViewsList.get(position);
        }

        @Override
        public int getCount() {
            return mImageViewsList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }

        @Override
        public void finishUpdate(View arg0) {
        }
    }

    private class MyPageChangeListener implements OnPageChangeListener {
        boolean isAutoPlay = false;
        @Override
        public void onPageScrollStateChanged(int arg0) {
            switch (arg0) {
                case 1:// 手势滑动，空闲中
                    isAutoPlay = false;
                    break;
                case 2:// 界面切换中
                    isAutoPlay = true;
                    break;
                case 0:// 滑动结束，即切换完毕或者加载完毕
                    // 当前为最后一张，此时从右向左滑，则切换到第一张
                    if (mViewPager.getCurrentItem() == mViewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
                        // mViewPager.setCurrentItem(0);
                        destory();
                    }
                    // 当前为第一张，此时从左向右滑，则切换到最后一张
                    else if (mViewPager.getCurrentItem() == 0 && !isAutoPlay) {
                        // mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1);
                        destory();
                    }
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int pos) {
            mCurrentItem = pos;
            for (int i = 0; i < mDotViewsList.size(); i++) {
                if (i == pos) {
                    ((View)mDotViewsList.get(pos)).setBackgroundResource(
                        R.drawable.dot_black);
                } else {
                    ((View)mDotViewsList.get(i)).setBackgroundResource(
                        R.drawable.dot_white);
                }
            }
        }
    }

    private class SlideShowTask implements Runnable {
        @Override
        public void run() {
            synchronized (mViewPager) {
                mCurrentItem = (mCurrentItem + 1) % mImageViewsList.size();
                mHandler.obtainMessage().sendToTarget();
            }
        }

    }
    private void destoryBitmaps() {
        for (int i = 0; i < IMAGE_COUNT; i++) {
            ImageView imageView = mImageViewsList.get(i);
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                drawable.setCallback(null);
            }
        }
    }

    private void destory() {
        ((ViewGroup) getParent()).removeView(this);
        destoryBitmaps();
    }
}
