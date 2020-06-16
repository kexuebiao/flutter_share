// Copyright 2019 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.share;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles share intent.
 */
class Share {

    private Activity activity;

    /**
     * Constructs a Share object. The {@code activity} is used to start the share intent. It might be
     * null when constructing the {@link Share} object and set to non-null when an activity is
     * available using {@link #setActivity(Activity)}.
     */
    Share(Activity activity) {
        this.activity = activity;
    }

    /**
     * Sets the activity when an activity is available. When the activity becomes unavailable, use
     * this method to set it to null.
     */
    void setActivity(Activity activity) {
        this.activity = activity;
    }

    void share(String text, String subject) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Non-empty text expected");
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.setType("text/plain");

        List<ResolveInfo> resInfo = activity.getPackageManager().queryIntentActivities(shareIntent, 0);
        List<Intent> targetedShareIntents = new ArrayList<>();
        for (ResolveInfo info : resInfo) {

            Intent targeted = new Intent(Intent.ACTION_SEND);
            targeted.setType("text/plain");
            ActivityInfo activityInfo = info.activityInfo;
            Log.d("package", String.format("packageName:%s,name:%s", activityInfo.packageName,
                    activityInfo.name));

            if (activityInfo.packageName.contains("com.android.mms")
                    || activityInfo.packageName.contains("com.android.email")
                    || (activityInfo.packageName.contains("com.tencent.mm")&&activityInfo.name.contains("com.tencent.mm.ui.tools.ShareImgUI"))
                    || (activityInfo.packageName.contains("com.tencent.mobileqq")&&activityInfo.name.contains("com.tencent.mobileqq.activity.JumpActivity"))
                    || activityInfo.packageName.contains("notes")
                    || activityInfo.packageName.contains("notepad")
            ) {

                targeted.putExtra(Intent.EXTRA_TITLE, subject);
                targeted.putExtra(Intent.EXTRA_SUBJECT, subject);
                targeted.putExtra(Intent.EXTRA_TEXT, text);
                targeted.setClassName(activityInfo.packageName, activityInfo.name);
                targetedShareIntents.add(targeted);
            }
        }

        Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0),
                subject);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                targetedShareIntents.toArray(new Parcelable[]{}));

        if (activity != null) {
            activity.startActivity(chooserIntent);
        } else {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(chooserIntent);
        }
    }
}
