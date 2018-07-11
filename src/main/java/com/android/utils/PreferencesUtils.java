package com.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Description : 配置文件
 * User: chenyanmo(master@chenyanmo.com)
 * Date: 2016/10/9
 * Time: 13:54
 */
public class PreferencesUtils
{
	public static final String APP_CONFIG = "app_config"; // 配置文件不删除的
	private Context mCtx;
	private Editor mEditor;
	private SharedPreferences mPreferences;
	private static PreferencesUtils instance;
	public static PreferencesUtils getInstance(Context mContext){
		if (instance == null){
			instance = new PreferencesUtils(mContext);
		}
		return instance;
	}

	/**
	 *  根据文件名，取配置文件
	 * @param mContext
	 * @param preferenceName
     * @return
     */
	public static PreferencesUtils getInstance(Context mContext, String preferenceName){
		if (instance == null){
			instance = new PreferencesUtils(mContext, preferenceName);
		}
		return instance;
	}

	/**
	 *  获取取配置文件
	 * @param context
	 * @param preferenceName
     */
	public PreferencesUtils(Context context, String preferenceName)
	{
		mCtx = context;
		mPreferences = mCtx.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
	}

	// 不包含文件名，取默认配置文件
	public PreferencesUtils(Context context)
	{
		mCtx = context;
		mPreferences = PreferenceManager.getDefaultSharedPreferences(mCtx);
	}

	public boolean getBoolean(String key, boolean defValue)
	{
		return mPreferences.getBoolean(key, defValue);
	}

	public void putBoolean(String key, boolean state)
	{
		mEditor = mPreferences.edit();
		mEditor.putBoolean(key, state);
		mEditor.commit();
	}

	public String getString(String key, String defValue)
	{
		return mPreferences.getString(key, defValue);
	}

	public void putString(String key, String value)
	{
		mEditor = mPreferences.edit();
		mEditor.putString(key, value);
		mEditor.commit();
	}

	public int getInt(String key, int defValue)
	{
		return mPreferences.getInt(key, defValue);
	}

	public void putInt(String key, int value)
	{
		mEditor = mPreferences.edit();
		mEditor.putInt(key, value);
		mEditor.commit();
	}

	public void putLong(String key, long value)
	{
		mEditor = mPreferences.edit();
		mEditor.putLong(key, value);
		mEditor.commit();
	}

	public void remove(String key)
	{
		mEditor = mPreferences.edit();
		mEditor.remove(key);
		mEditor.commit();
	}

	public long getLong(String key, Long defValue)
	{
		return mPreferences.getLong(key, defValue);
	}

	public boolean contains(String key)
	{
		return mPreferences.contains(key);
	}

	public boolean clear()
	{
		mEditor = mPreferences.edit();
		this.mEditor.clear();
		return mEditor.commit();
	}

	public LinkedHashSet<String> getStringSet(String key)
	{
		String regularEx = "#";
		LinkedHashSet<String> stringSet = new LinkedHashSet<String>();
		String[] str = null;
		String values;
		values = mPreferences.getString(key, "");
		if (values != null && !"".equals(values.trim()) && !"null".equals(values.trim())){
			str = values.split(regularEx);
			stringSet = new LinkedHashSet<String>(Arrays.asList(str));
		}
		return stringSet;
	}

	public void putStringSet(String key, LinkedHashSet<String> values)
	{

		String regularEx = "#";
		String str = "";
		mEditor = mPreferences.edit();
		mEditor.commit();
		if (values != null)
		{
			if (values.size() > 0)
			{
				for (String value : values)
				{
					str += value;
					str += regularEx;
				}
				mEditor.putString(key, str);
				mEditor.commit();
			} else
			{
				mEditor.putString(key, "");
				mEditor.commit();
			}
		}

	}

}
