package com.KDJStudios;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Util {
	// given a content or file uri, return a file path
	public static String uriToFilePath(Context context, String contentUri) {
		if(Uri.parse(contentUri).getScheme().equals("content")){
			String[] p={MediaStore.MediaColumns.DATA};
			Cursor cursor = context.getContentResolver().query(
					Uri.parse(contentUri),
					p, // which columns
					null, // which rows (all rows)
					null, // selection args (none)
					null); // order-by clause (ascending by name)
			if(cursor != null){
				int iColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
				if(cursor.moveToFirst()){
					return(cursor.getString(iColumn));
				}
			}
		}
		if(Uri.parse(contentUri).getScheme().equals("file")){
			return(Uri.parse(contentUri).getPath());
		}
		return(null);
	}
	// given a content or file uri, return its title
	public static String uriTitle(Context context, String contentUri) {
		String result = null;
		String[] p={MediaStore.MediaColumns.TITLE};
		Uri uri = Uri.parse(contentUri);
		if(!uri.toString().equals("content")){
			return(fileUriTitle(context, contentUri));
		}
		Cursor cursor = context.getContentResolver().query(
				uri,
				p, // which columns
				null, // which rows (all rows)
				null, // selection args (none)
				null); // order-by clause (ascending by name)
		if(cursor != null){
			int iColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE);
			if(cursor.moveToFirst()){
				result = cursor.getString(iColumn);
			}
		}
		return(result);
	}
	// given a file uri, return its title
	private static String fileUriTitle(Context context, String contentUri) {
		String result = null;
		String[] p={MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.TITLE};
		Uri uri = Uri.parse(contentUri);
		String path = uri.getPath();
		String last = Uri.parse(path).getLastPathSegment();
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
				p, // which columns
				MediaStore.MediaColumns.DISPLAY_NAME + "='" + last +  "'", // which rows
				null, // selection args (none)
				null); // order-by clause (ascending by name)
		if(cursor != null){
			int tcol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE);
			if(cursor.moveToFirst()){
				result = cursor.getString(tcol);
			}
		}
		return(result);
	}
// given a file path, return a bitmap
	public static Bitmap imageFilePathToBitmap(Context c, String path, int maxDim){
		Bitmap bmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		try {
			// compute the smallest size bitmap we need to read
			BitmapFactory.decodeStream(new FileInputStream(path), null, opts);
			int w = opts.outWidth;
			int h = opts.outHeight;
			int s = 1;
			while(true) {
				if((w/2 < maxDim) || (h/2 < maxDim)){
					break;
				}
				w /= 2;
				h /= 2;
				s++;
			}
			// scale and read the data
			opts.inJustDecodeBounds = false;
			opts.inSampleSize = s;
			bmp = BitmapFactory.decodeStream(new FileInputStream(path), null, opts);
		} catch (FileNotFoundException e) {
			Log.w("LOADING IMAGE FILE FAIL", e.getMessage());
		}
		return(bmp);
	}
	}