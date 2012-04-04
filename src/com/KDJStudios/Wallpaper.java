package com.KDJStudios;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/*
 * This animated wallpaper draws sprites.
 */
public class Wallpaper extends WallpaperService {

    private final Handler 			mHandler = new Handler();
	public static final String 		SHARED_PREFS="hglwsettings";
	public static final String 		VOID="DEFAULT";
    public static final int 		BG_COLOR = 0x000037;
	public static final int 		FG_COLOR = 0xffffffff;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new SpriteEngine(this);
    }

    class SpriteEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
    	private final ArrayList<Sprite>		mSprites = new ArrayList<Sprite>();
    	private int 						mSpriteSize=0;
    	//frames
	    private static final int 		SPRITE_LIFETIME = 400;
		// the minimum number of circles we keep on the screen
		private static final int 		MIN_SPRITE = 3;
		// after this many we start killing them off whenever
		//  we create a new one
		private static final int 		MAX_SPRITE = 15;
		// radius limits
		private static final int 		MIN_RADIUS = 40;
		private static final int 		MAX_RADIUS = 100;	
		// time between frames (mili-sec)
		private static final int		FRAME_INTERVAL = 20;
		//Colors
        private final Paint 			mPaint = new Paint();
        private int						mBgColor = BG_COLOR;
    	private int						mSpriteColor = FG_COLOR;
    	private int						mTracerColor = FG_COLOR;
        private final float[]			pos = {0.6f, 1.0f};
    	private final int[]				colors = {BG_COLOR, FG_COLOR};
     	//Screen and Movements
    	private float 					mWidth;
        private float 					mHeight;
        private float					mXOff = 0f;
    	private float					mYOff = 0f;
    	private int						mXOffPix = 0;
    	private int						mYOffPix = 0;
        private float 					mTouchX = -1;
        private float 					mTouchY = -1;
        //Allow Settings
        private SharedPreferences		mPrefs = null;
        //
    	//Sprite Image
    	private Bitmap					mSpriteBitmap =null;
        private boolean					mShowSprites = false;
        private String					mSpriteImageString;
        private boolean					mCollisions = true;
    	private int						mTracerStyle = 0;    	
    	@SuppressWarnings("unused")
		private int						mPhysicStyle = 0;
    	private int						mRotationStyle = 0;
      //BG Image
    	private Bitmap					mBitmap = null;
    	private String					mBgImageString;
    	private boolean					mShowBgImage = false;
    	//private ColorFilter 			filter;
        private final Runnable mNextFrame = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        private boolean mVisible;

        SpriteEngine(WallpaperService ws) {
            //Temporary testing
            mBgImageString=VOID;
            mSpriteImageString=VOID;
            
            mPrefs = Wallpaper.this.getSharedPreferences(SHARED_PREFS, 0);
    		mPrefs.registerOnSharedPreferenceChangeListener(this);
    		onSharedPreferenceChanged(mPrefs, null);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            // By default we don't get touch events, so enable them.
            setTouchEventsEnabled(true);
        }
        
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested){
    		return (super.onCommand(action, x, y, z, extras, resultRequested));
    	}
        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mNextFrame);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mNextFrame);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            drawFrame();
            //
            if((mWidth != width)||(mHeight != height)){
            	mWidth = width;
            	mHeight = height;
            	loadSpriteBitmap();
                loadBgBitmap();
            }

    		mSpriteSize=mSprites.size();
    		if(mSpriteSize < MIN_SPRITE){
    			for(int x=mSpriteSize;x<MIN_SPRITE;x++){
    				mSprites.add(generateSprite(mWidth/2, mHeight/2, mWidth/2, mHeight/2));
    			}
    		}
    		
        }
        
    	
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            //delete sprites
            mSprites.clear();
            mSpriteSize=0;
            mVisible = false;
            mHandler.removeCallbacks(mNextFrame);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
            mXOff = xOffset;
    		mYOff = yOffset;
    		if(mBitmap != null){
    			mXOffPix = (int) (mXOff * (mWidth - mBitmap.getWidth()));
    			mYOffPix = (int) (mYOff * (mHeight - mBitmap.getHeight()));
    		}
            drawFrame();
        }

        /*
         * Store the position of the touch event
         * Spawn a new sprite if alloted memory in "mSprites" is less then "MAX_SPRITE"
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
        	if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchX = event.getX();
                mTouchY = event.getY();
            }else  if (event.getAction() == MotionEvent.ACTION_UP) {
        		if(mSprites.size()<MAX_SPRITE){
    	        	Sprite b = generateSprite( event.getX(), event.getY(),mTouchX, mTouchY);//
    				mSprites.add(b);
    				mSpriteSize++;
            	}
        		mTouchX = -1;
                mTouchY = -1;
            }else {
                mTouchX = -1;
                mTouchY = -1;
            }
        	
        	
            super.onTouchEvent(event);
        }
        /*
         * Generate the sprite and its velocity
         */
        Sprite generateSprite(float x1, float y1, float x2, float y2){
    		Random rn = new Random();
    		int r = (int) (MAX_RADIUS * rn.nextDouble());
    		if(r <= MIN_RADIUS){
    			r = MIN_RADIUS;
    		}
    		int m = r / 2;
    		int vx = (int) (x2 - x1);
    		int vy = (int) (y2 - y1);
    		Sprite b = new Sprite((int)x1, (int)y1, r, m,mSpriteBitmap);
    		b.setRotation((int) (360 * rn.nextDouble()));
    		
    		setV(b, vx, vy, rn);
    		return(b);
    	}
    	void setV(Sprite sprite, int vx, int vy, Random r) {
    		int[] signs = {-1, 1};
    		vx /= (mWidth / 20);
    		vy /= (mHeight / 20);
    		if(vx == 0){
    			vx = signs[r.nextInt(2)];
    		}
    		if((vx < 5) && (vx > -5)){
    			vx *= 5;
    		}
    		if((vy < 5) && (vy > -5)){
    			vy = (int) ((mHeight / 60) * r.nextDouble());
    			vy *= signs[r.nextInt(2)];
    		}
    		sprite.setV(vx, vy);
    		return;
    	}

        /*
         * Draw one frame of the wallpaper. 
         * Check for bad memory
         * Schedule the next frame
         */
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            
            try {
                c = holder.lockCanvas();
                if (c != null) {
                	/*
                     * Order to draw the Screen
                     * BG->Tracers->Sprites->TouchPoint
                     */
                	
                    drawBG(c);
                    drawTracers(c);
       				drawSprites(c);
                    drawTouchPoint(c);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
    		// check for bad memory no matter if the canvas was used
            checkSprites();
    		// schedule the next frame
            mHandler.removeCallbacks(mNextFrame);
            if (mVisible) {
                mHandler.postDelayed(mNextFrame, FRAME_INTERVAL);
            }
        }
        /*
         * Draws tracer effects at 
         */
		void drawTracers(Canvas c){
			//check for collisions
			if(mShowSprites){
			    if(mTracerStyle!=0){
		        	for(Sprite b: mSprites){
						b.drawTracers(c, mPaint, mTracerColor,mTracerStyle);
					}
	    		}
			}
		}
		/*
         * Draws a the sprites loaded into "mSprites"
         * Also handles physics, rotations, and collisions here
         */
        void drawSprites(Canvas c){

    		if((mShowSprites)){
    			for(Sprite b : mSprites){
    				//check for collisions
    				b.collide((int)mWidth,(int) mHeight);
        			if(mCollisions){
    	    			for(Sprite b2 : mSprites){
    	    				if(b2 != b){
    	    					int state = b2.collide(b);
    	    					if((state == Sprite.START_COLLISION)){
    	    						//OHH NO Collision!
    	    					}
    	    				}
    	    			}
    	    		}
        			//Perform updates
    				b.update();
    				if((mRotationStyle==1)||(mRotationStyle==3)){//Rotate? both
						b.updateRotation();
					}else if((mRotationStyle==2)||(mRotationStyle==3)){//Flip? both
						//b.updateFlip();
					}
    				//Draw
					b.draw(c, mPaint);
    			}
			}else{
				c.drawCircle((int)mTouchX, (int)mTouchY, (int)80, mPaint);
			}
        }
        /*
         * Double check for dead sprites, can't waste that battery
         */
        void checkSprites(){
        	mSpriteSize = mSprites.size();
    		Sprite oldestSprite = null;
    		int oldestAge = 0;
    		int thisAge = 0;
    		if(mSpriteSize > MIN_SPRITE){
    			List<Sprite> toDeleteList = new ArrayList<Sprite>();
    			for(Sprite b : mSprites){
    				thisAge = b.getAge();
    				// remove the old ones
    				if(thisAge >= SPRITE_LIFETIME){
    					toDeleteList.add(b);
    				}
    				// if we didnt remove it then it might be the oldest living
    				else if(thisAge > oldestAge){
    					oldestAge = thisAge;
    					oldestSprite = b;
    				}
    			}
    			mSprites.removeAll(toDeleteList);
    			mSpriteSize = mSprites.size();
    			// if there's still too many then remove the oldest living
    			if(mSpriteSize > MAX_SPRITE){
    				if(oldestSprite != null){
    					mSprites.remove(oldestSprite);
    					mSpriteSize--;
    				}
    			}
    		}
    		
        }
        
        /*
         * Draws the background to the canvas
         */
        void drawBG(Canvas c){

        /*  if we need a bg image and haven't loaded it, then try to load it
    		if(mShowBgImage  && (mBgImageString != null)){
    			if(mBitmap == null){
    				loadBgBitmap();
    			}
    		}
    		*/
        	// fade-erase
			if((mBitmap != null)&&(mShowBgImage)){
				
				//mPaint.setColor(mBgColor);
				//filter = new LightingColorFilter(mBgColor, 1);
				//mPaint.setColorFilter(filter);
				c.drawBitmap(mBitmap, mXOffPix, mYOffPix, mPaint);
			}else {
				c.drawARGB(255,0,0,mBgColor);
			}
        }
        /*
         * Draws a circle around the current touch point
         */
        void drawTouchPoint(Canvas c) {
            if (mTouchX >=0 && mTouchY >= 0) {
            	mPaint.setShader(new RadialGradient((int)mTouchX, (int)mTouchY, (int)80, colors, pos, Shader.TileMode.CLAMP));
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(Wallpaper.FG_COLOR);
    			c.drawCircle((int)mTouchX, (int)mTouchY, (int)80, mPaint);
    			mPaint.setStyle(Paint.Style.STROKE);
    			mPaint.setShader(null);
            }
        }
        /*
         *  Used to load "wallpapers bitmaps" 
         *  Note sprite makes its own scaled copy so no need for a scale function here
         */
        void loadSpriteBitmap() {
    		Bitmap bmp = null;
    		//load default
	        bmp=BitmapFactory.decodeResource(getResources(),R.drawable.s04);

			if(mSpriteImageString!=VOID){
				bmp = Util.imageFilePathToBitmap(getBaseContext(), mSpriteImageString, (int) MAX_RADIUS);
			}
			//Now we should have a valid image but check just in case! :)
			if(bmp != null){
				mSpriteBitmap = bmp;
			}
    		//clear all known sprites so we make new ones with the new image!!
    		mSprites.clear();
            mSpriteSize=0;
    		return;
    	}
       void loadBgBitmap() {
    		Bitmap bmp = null;
    		bmp=BitmapFactory.decodeResource(getResources(),R.drawable.bg14);
    		if((mWidth > 0) && (mHeight > 0)){
    			
    			//load from preference or user choice by string value;
    			if(mBgImageString!=VOID){
    				bmp = Util.imageFilePathToBitmap(getBaseContext(), mBgImageString, (int) Math.max(mWidth, mHeight));
    			}
    			//Now we should have a valid image but check just in case! :)
    			if(bmp != null){
    				mBitmap = scaleBgBitmap(bmp);//scale and save to final bitmap
    			}
    			//adjust so its centered
    			if(mBitmap != null){
    				mXOffPix = (int) (mXOff * (mWidth - mBitmap.getWidth()));
    				mYOffPix = (int) (mYOff * (mHeight - mBitmap.getHeight()));
    			}
    		}
    		return;
    	}
    private	Bitmap scaleBgBitmap(Bitmap b) {
    		Bitmap result = null;
    		int bw = b.getWidth();
    		int bh = b.getHeight();
    		double s = (double)mHeight / (double)bh;
    		int newW = (int)(bw * s);
    		if(newW < mWidth){
    			newW = (int) mWidth;
    		}
    		result = Bitmap.createScaledBitmap(b, newW, (int) mHeight, false);
    		
    		return(result);
    	}
    	/*
    	 *  check for any updates in the user settings/prefs
    	 */
    	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
    		if(key != null){
    			//Background
    			if(key.equals("showBgImagePref")){
    				changeShowBgImagePref(prefs.getBoolean("showBgImagePref", false));
    			}else if(key.equals("stockBgImagePref")){
    				changeBgImagePref(prefs.getString("stockBgImagePref", "0"));
    			}else if(key.equals(Settings.BG_IMAGE_KEY)){
    				changeBgImagePref(prefs.getString(Settings.BG_IMAGE_KEY, VOID));
    			}else if(key.equals("bgColorPref")){
    				changeBgColorPref(prefs.getInt("bgColorPref", mBgColor));
    			}//Sprites
    			else if(key.equals("showSpritesPref")){
    				changeShowSpritePref(prefs.getBoolean("showSpritesPref", false));
    			}else if(key.equals("stockSpritePref")){
    				changeSpriteImagePref(prefs.getString("stockSpriteImagePref", "0"));
    			}else if(key.equals(Settings.SPRITE_IMAGE_KEY)){
    				changeSpriteImagePref(prefs.getString(Settings.SPRITE_IMAGE_KEY, VOID));
    			}else if(key.equals("spriteColorPref")){
    				changeBgColorPref(prefs.getInt("spriteColorPref", mSpriteColor));
    			}
    			//
    			else if(key.equals("rotationStylePref")){
    				changeRotationStylePref(prefs.getString("rotationStylePref", null));
    			}
    			else if(key.equals("collisionPref")){
    				changeCollisionPref(prefs.getBoolean("collisionPref", false));
    			}
    			else if(key.equals("physicStylePref")){
    				changePhysicStylePref(prefs.getString("physicStylePref", null));
    			}
    			else if(key.equals("tracerStylePref")){
    				changeTracerStylePref(prefs.getString("tracerStylePref", null));
    			}
    			else if(key.equals("tracerColorPref")){
    				changeTracerColorPref(prefs.getInt("tracerColorPref", mTracerColor));
    			}
    			else if(key.equals("resetPref")){
    				changeResetPref();
    			}
    			
    			//Stuffs
    			
    		}else {//Defaults right lol
    			//Background Images
    			changeShowBgImagePref(prefs.getBoolean("showBgImagePref", true));
    			changeBgImagePref(prefs.getString(Settings.BG_IMAGE_KEY, VOID));
    			//Use sprites   			
    			changeShowSpritePref(prefs.getBoolean("showSpritesPref", true));
    			changeSpriteImagePref(prefs.getString(Settings.SPRITE_IMAGE_KEY, VOID));
    			//Sprite Stuff
    			changeTracerStylePref(prefs.getString("tracerStylePref", "0"));
    			changeCollisionPref(prefs.getBoolean("collisionPref", true));
    			changePhysicStylePref(prefs.getString("physicStylePref", "0"));
    			changeRotationStylePref(prefs.getString("rotationStylePref", "0"));
    			//Color Settings
    			changeBgColorPref(prefs.getInt("bgColorPref", mBgColor));				
    			changeSpriteColorPref(prefs.getInt("spriteColorPref", mSpriteColor));
    			changeTracerColorPref(prefs.getInt("tracerColorPref", mTracerColor));
}

    		return;
    	}
    	private void changeBgColorPref(int value) {
    		mBgColor = value;
    		return;
    	}
    	private void changeSpriteColorPref(int value) {
    		mSpriteColor = value;
    		Sprite.setColor(mSpriteColor);
    		return;
    	}
    	private void changeTracerColorPref(int value) {
    		mTracerColor = value;
    		//Sprite.setTracerColor(mTracerColor);
    		return;
    	}
    	private void changeShowSpritePref(boolean value){
    		mShowSprites = value;
    		return;
    	}
    	private void changeSpriteImagePref(String value){
    		mSpriteImageString = value;
  			loadSpriteBitmap();
    		return;
    	}
    	private void changeTracerStylePref(String value){
    		mTracerStyle = Integer.parseInt(value);
    		Sprite.setTracerStyle(mTracerStyle);
    		return;
    	}
    	private void changeCollisionPref(boolean value){
    		mCollisions = value;
    		return;
    	}
    	private void changePhysicStylePref(String value){
    		mPhysicStyle = Integer.parseInt(value);
    		//Sprite.setPhysicStyle(mPhysicStyle);
    		return;
    	}
    	private void changeRotationStylePref(String value){
    		mRotationStyle = Integer.parseInt(value);
    		//Sprite.setRotationStyle(mRotationStyle);
    		return;
    	}
    	private void changeShowBgImagePref(boolean value){
    		mShowBgImage = value;
    		return;
    	}
    	private void changeBgImagePref(String value){
    		mBgImageString = value;
    		loadBgBitmap();
    		return;
    	}
    	private void changeResetPref(){
    		mBgImageString = VOID;
    		mSpriteImageString = VOID;
    		loadSpriteBitmap();
    		loadBgBitmap();
    		
    		return;
    	}
    }
}
