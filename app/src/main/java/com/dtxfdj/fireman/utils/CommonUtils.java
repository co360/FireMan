// Copyright 2019 The dtxfdj. All rights reserved.
// Author: baidaogui.

package com.dtxfdj.fireman.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import com.dtxfdj.fireman.R;
import java.util.List;

public class CommonUtils {

    public static Bitmap decodeSampledBitmapFromResource(
            Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(
                options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static boolean isValidUrl(String url) {
        return !TextUtils.isEmpty(url)
                && (url.startsWith("http://")
                || url.startsWith("https://")
                || url.startsWith("about:"));
    }

    public static boolean handleNoneBrowserUrl(
            Activity activity, String url) {
        if (url.startsWith("weixin://wap/pay?")) {
            if (isWxInstall(activity)) {
                return startActivity(activity, url);
            } else {
                Toast.makeText(activity,
                        R.string.not_install_weixin,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (url.startsWith("alipays://platformapi/startApp?")) {
            if (isAliPayInstalled(activity)) {
                return startActivity(activity, url);
            } else {
                Toast.makeText(activity,
                        R.string.not_install_alipay,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if(url.startsWith("tel:")) {
            Intent sendIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            activity.startActivity(sendIntent);
            return true; // 否则键盘回去，页面显示"找不到网页"
        }
        return false;
    }

    private static boolean isWxInstall(Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            List pinfo = packageManager.getInstalledPackages(0);
            if (pinfo == null) {
                return false;
            }
            for (int i =0; i < pinfo.size(); i++) {
                String pn = ((PackageInfo) pinfo.get(i)).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isAliPayInstalled(Context context) {
        ComponentName componentName = null;
        try {
            Uri uri = Uri.parse("alipays://platformapi/startApp");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            componentName =
                    intent.resolveActivity(context.getPackageManager());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return componentName != null;
    }

    private static boolean startActivity(Context context, String url) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}