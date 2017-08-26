/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 检测按下的位置和移动的位置，计算偏移量，满足（移动条件，并且y方向的偏移量》x方向上的偏移量。说明移动了）在松手的时候，判断移动的范围是否满足大于最小抛动速度
 */
public class VerticalFlingDetector implements View.OnTouchListener {

    private static final float CUSTOM_SLOP_MULTIPLIER = 2.2f;
    private static final int SEC_IN_MILLIS = 1000;

    private VelocityTracker mVelocityTracker;
    private float mMinimumFlingVelocity;
    private float mMaximumFlingVelocity;
    private float mDownX, mDownY;
    private boolean mShouldCheckFling;
    private double mCustomTouchSlop;

    public VerticalFlingDetector(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        mMinimumFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = vc.getScaledMaximumFlingVelocity();
        // 增大移动的不敏感度
        mCustomTouchSlop = CUSTOM_SLOP_MULTIPLIER * vc.getScaledTouchSlop();
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                //按下的时候，将检测抛动设置为false，相当于重置
                mShouldCheckFling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // 省的影响下面的判断，
                if (mShouldCheckFling) {
                    break;
                }
                // 当y方向移动范围大于最小值并且y的偏移量大于X的偏移量的时候检测抛动
                if (Math.abs(ev.getY() - mDownY) > mCustomTouchSlop &&
                        Math.abs(ev.getY() - mDownY) > Math.abs(ev.getX() - mDownX)) {
                    // 只要有一次满足条件即可
                    mShouldCheckFling = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mShouldCheckFling) {
                    // 计算一秒移动的像素
                    mVelocityTracker.computeCurrentVelocity(SEC_IN_MILLIS, mMaximumFlingVelocity);
                    // only when fling is detected in down direction
                    // 如果获得的速度大于最小速度，说明是抛动
                    if (mVelocityTracker.getYVelocity() > mMinimumFlingVelocity) {
                        cleanUp();
                        return true;
                    }
                }
                // fall through.
                // 清空
            case MotionEvent.ACTION_CANCEL:
                cleanUp();
        }
        return false;
    }

    private void cleanUp() {
        if (mVelocityTracker == null) {
            return;
        }
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }
}
