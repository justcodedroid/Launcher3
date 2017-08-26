/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.launcher3.accessibility;

import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.android.launcher3.Launcher;
// 事件选中的状态也是通过handler来发送的
/**
 * Periodically sends accessibility events to announce ongoing state changed. Based on the
 * implementation in ProgressBar.
 */
public class DragViewStateAnnouncer implements Runnable {
    //200ms后处理View状态的改变
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;

    private final View mTargetView;

    private DragViewStateAnnouncer(View view) {
        mTargetView = view;
    }
    // announce 发布
    public void announce(CharSequence msg) {
        // 给view设置一个描述，描述这个view是做什么的
        mTargetView.setContentDescription(msg);
        // 移除post发送的事件
        mTargetView.removeCallbacks(this);
        mTargetView.postDelayed(this, TIMEOUT_SEND_ACCESSIBILITY_EVENT);
    }

    public void cancel() {
        mTargetView.removeCallbacks(this);
    }

    @Override
    public void run() {
        // 发送选中状态，修改状态选择器
        mTargetView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
    }

    public void completeAction(int announceResId) {
        cancel();
        Launcher launcher = Launcher.getLauncher(mTargetView.getContext());
        launcher.getDragLayer().announceForAccessibility(launcher.getText(announceResId));
    }

    public static DragViewStateAnnouncer createFor(View v) {
        if (((AccessibilityManager) v.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE))
                .isEnabled()) {
            return new DragViewStateAnnouncer(v);
        } else {
            return null;
        }
    }
}
