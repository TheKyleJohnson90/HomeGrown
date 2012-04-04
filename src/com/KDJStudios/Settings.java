package com.KDJStudios;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
	
	private static final String TAG = "com.KDJStudios.Settings";
	private static final String IMAGE_MIME_TYPE = "image/*";
	private static final String UNKNOWN_TITLE = "(unknown)";
	//actual image file
	public static final String	BG_IMAGE_KEY = "bgImagePref";
	public static final String	SPRITE_IMAGE_KEY = "spriteImagePref";
	//stock list in prefs
	public static final String	BG_LIST_KEY = "stockBgImagePref";
	public static final String	SPRITE_LIST_KEY = "stockSpriteImagePref";
	//choice lists
	public static final String	TRACER_LIST_KEY = "tracerStylePref";
	//public static final String	PHYSIC_LIST_KEY = "physicStylePref";


	private static final int	CHOOSE_IMAGE_REQUEST = 1;
	private static final int	CHOOSE_SPRITE_IMAGE_REQUEST = 2;
	//private static final int	CHOOSE_TRACER_REQUEST = 3;
	//private static final int	CHOOSE_SOUND_REQUEST = 2;
	//public static final String	RESET_KEY = "resetPref";
	private ListPreference		bgList = null;
	private ListPreference		spriteList = null;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getPreferenceManager().setSharedPreferencesName(Wallpaper.SHARED_PREFS);
		addPreferencesFromResource(R.xml.prefs);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		findPreference(BG_IMAGE_KEY).setOnPreferenceClickListener(this);
		//bgList = (ListPreference) findPreference(BG_LIST_KEY);
		//updateList(BG_IMAGE_KEY);
		
		findPreference(SPRITE_IMAGE_KEY).setOnPreferenceClickListener(this);
		//spriteList = (ListPreference) findPreference(SPRITE_LIST_KEY);
		//updateList(SPRITE_IMAGE_KEY);
		//findPreference(RESET_KEY).setOnPreferenceChangeListener(this);
		return;
	}

	@Override
	protected void onResume() {
		super.onResume();
		return;
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
		return;
	}

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		return;
	}


	@SuppressWarnings("unused")
	private void updateList(String key) {
		//String 
		String vUser = findPreference(key).getSharedPreferences().getString(key, null);
		if(vUser != null){
			String title = Util.uriTitle(getBaseContext(), vUser);
			updateList(key, title, vUser, false);
		}
		return;
	}
	
	private void updateList(String key, String t, String uri, boolean selectFlag) {
		ListPreference mList;
		
		if(key==SPRITE_IMAGE_KEY){
			mList=spriteList;
		}else if(key==BG_IMAGE_KEY){
			mList=bgList;
		}else{
			return;
		}
		int mListOrigSize=mList.getEntries().length;
		CharSequence[] keys = mList.getEntries();
		CharSequence[] values = mList.getEntryValues();
		int i = keys.length;
		String title = (t == null) ? UNKNOWN_TITLE : t;
		if(i > mListOrigSize){
			keys[i-1] = title;
			values[i-1] = uri;
		}
		else {
			CharSequence[] newKeys = new CharSequence[i+1];
			CharSequence[] newValues = new CharSequence[i+1];
			for(int j = 0; j < i; j++){
				newKeys[j] = keys[j];
				newValues[j] = values[j];
			}
			newKeys[i] = title;
			newValues[i] = uri;
			keys = newKeys;
			values = newValues;
			i++;
		}
		mList.setEntries(keys);
		mList.setEntryValues(values);
		if(selectFlag){
			mList.setValueIndex(i-1);
		}
		if(key==SPRITE_IMAGE_KEY){
			spriteList=mList;
		}else if(key==BG_IMAGE_KEY){
			bgList=mList;
		}		
		return;
	}

	public boolean onPreferenceClick(Preference pref) {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		int requestCode = 0;
		if(pref.getKey().equals(BG_IMAGE_KEY)){
			i.setType(IMAGE_MIME_TYPE);
			requestCode = CHOOSE_IMAGE_REQUEST;
		}else if(pref.getKey().equals(SPRITE_IMAGE_KEY)){
			i.setType(IMAGE_MIME_TYPE);
			requestCode = CHOOSE_SPRITE_IMAGE_REQUEST;
		}
		else {
			return(false);
		}
		try {
			startActivityForResult(i, requestCode);
		} catch (ActivityNotFoundException e) {
			Log.w(TAG, e.getMessage());
		}
		return(true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if((resultCode == Activity.RESULT_OK) && (requestCode == CHOOSE_IMAGE_REQUEST) && (data != null)) {
			String imagePath = Util.uriToFilePath(getBaseContext(), data.toUri(0));
			if(imagePath != null){
				findPreference(BG_IMAGE_KEY).getEditor().putString(BG_IMAGE_KEY, imagePath).commit();
			}
		}else if((resultCode == Activity.RESULT_OK) && (requestCode == CHOOSE_SPRITE_IMAGE_REQUEST) && (data != null)) {
			String imagePath = Util.uriToFilePath(getBaseContext(), data.toUri(0));
			if(imagePath != null){
				findPreference(SPRITE_IMAGE_KEY).getEditor().putString(SPRITE_IMAGE_KEY, imagePath).commit();
			}
		}
		
		return;
	}

}