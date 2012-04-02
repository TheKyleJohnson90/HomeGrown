package com.KDJStudios;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class MinMaxPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener
{
private static final String	androidns="http://schemas.android.com/apk/res/android";
	
	private SeekBar		mSeekBar;
	private Context		mContext;
	private int			mDefault;
	private int			mMin = 0;
	private int			mMax = 255;
	private int			mValue = 0;
	public MinMaxPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns,"max", 255);
		mMin = attrs.getAttributeIntValue(androidns,"min", 255);

	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// check if the "OK" button was pressed
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if(shouldPersist()){
				persistInt(mValue);
			}
		}
		return;
	}
	@Override 
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6,6,6,6);
		
		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.bottomMargin = 25;
		
		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.getProgressDrawable().setColorFilter(0xbbff0000, PorterDuff.Mode.SRC_OVER);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		if (shouldPersist()){
			mValue = getPersistedInt(mDefault);

		}

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		return(layout);
	}
	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if(restore){
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;

		}
		else {
			mValue = (Integer)defaultValue;

		}
		return;
	}
	
	@Override 
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);

		return;
	}
	public void onProgressChanged(SeekBar seek, int progress,
			boolean fromUser) {
		int v = progress;
		if(seek == mSeekBar){
			v = (mValue & 0xffff00) | (progress);
		}
		mValue = v;
		callChangeListener(new Integer(progress));
		return;
		
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		return;
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		return;
		
	}
	public void setMax(int max){
		mMax = max; 
		return;
	}

	public int getMax(){
		return(mMax);
	}
	public void setMin(int min){
		mMin = min; 
		return;
	}

	public int getMin(){
		return(mMin);
	}

}