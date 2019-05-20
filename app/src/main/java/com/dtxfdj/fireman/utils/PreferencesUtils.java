// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
// Author: baidaogui@sogou-inc.com.

package com.dtxfdj.fireman.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtils {

    private SharedPreferences mPreferences = null;
    private SharedPreferences.Editor mEditer = null;
    private static PreferencesUtils sInstance = null;

    // single instance
    private PreferencesUtils() {}

    public static PreferencesUtils getInstance() {
        if (sInstance == null) {
            sInstance = new PreferencesUtils();
        }
        return sInstance;
    }

    /**
     * 加载boolean类型值
     *
     * @param key
     *            待加载值的主键
     * @return 指定key的值, 不存在返回false
     */
    public boolean loadBoolean(Context context, String key) {
        return loadBoolean(context, key, true);
    }

    /**
     * 加载String类型值
     *
     * @param key
     *            待加载值的主键
     * @return 指定key的值, 不存在返回""
     */
    public String loadString(Context context, String key) {
        return loadString(context, key, "");
    }

    /**
     * 加载boolean类型值
     *
     * @param key
     *            待加载值的主键
     * @param defValue
     *            如果key不存在默认值
     * @return 指定key的boolean值
     */
    public boolean loadBoolean(Context context, String key, boolean defValue) {
        ensurePreferences(context);
        return mPreferences.getBoolean(key, defValue);
    }

    /**
     * 保存指定key的boolean类型值
     * @param key 待保存值的主键
     * @param value 待保存的boolean值
     */
    public void saveBoolean(Context context, String key, boolean value) {
        ensurePreferences(context);
        if (mEditer == null) {
            mEditer = mPreferences.edit();
        }
        mEditer.putBoolean(key, value);
        mEditer.apply();
    }

    /**
     * 加载boolean类型值
     *
     * @param key
     *            待加载值的主键
     * @param defValue
     *            如果key不存在默认值
     * @return 指定key的String值
     */
    public String loadString(Context context, String key,
            String defValue) {
        ensurePreferences(context);
        return mPreferences.getString(key, defValue);
    }

    /**
     * 保存指定key的String类型值
     * @param key 待保存值的主键
     * @param value 待保存的boolean值
     */
    public void saveString(Context context, String key, String value) {
        ensurePreferences(context);
        if (mEditer == null) {
            mEditer = mPreferences.edit();
        }
        mEditer.putString(key, value);
        mEditer.apply();
    }

    private void ensurePreferences(Context context) {
        if (mPreferences == null) {
            mPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
        }
    }
}