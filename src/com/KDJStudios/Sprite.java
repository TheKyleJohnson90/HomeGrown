package com.KDJStudios;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class Sprite {

	public static final int			NO_COLLISION = 0x00;
	public static final int			START_COLLISION = 0x01;
	public static final int			IN_COLLISION = 0x02;
	public static final int			ROTATION_RATE = 10;

	private static int				mColor = 0;
	private static int				mTracerColor = 0;
	private static float[]			pos = {0.6f, 1.0f};
	private static int[]			colors = {0, 0};
	
	private Set<Sprite> 			collisions = new HashSet<Sprite>();
	private List<Tracer> 			tracers = new ArrayList<Tracer>();
	private List<Tracer> 			donetracers = new ArrayList<Tracer>();
	private int 					mTracerCounter=0;
	private Bitmap					mBitmap = null;
	//you got to die
	private int						age;
	//position
	private double					centerX;
	private double					centerY;
	//velocity
	private double					vX;
	private double					vY;
	//bounding
	private double					radius;
	private double					mass;
	private int						mHeading;
	public static void setColor(int s){
		mColor = s;
	}
	public static void setTracerStyle(int s){
		mTracerColor = s;
	}
	public void setRotation(int s){
		mHeading=s;
	}
	public void updateRotation(){
		mHeading+=ROTATION_RATE;
		if(mHeading>=360){
			mHeading=0;
		}
	}
	
	Sprite(int x, int y){
		this(x, y, 1);
		return;
	}
	Sprite(int x, int y, int r){
		this(x, y, r, 1,null);
		return;
	}
	public Sprite(int x, int y, int r, int m,Bitmap b){
		colors[0] = Wallpaper.FG_COLOR;
		colors[1] = Wallpaper.BG_COLOR;
		vX = 0;
		vY = 0;
		centerX = x;
		centerY = y;
		radius = r;
		mass = m;
		age = 0;
		mBitmap =b;
		return;
	}
	private void scaleBitmap(int max) {
	       if((mBitmap.getWidth() != max)||(mBitmap.getHeight() != max)){
				mBitmap=Bitmap.createScaledBitmap(mBitmap,(int) max, (int) max, false);
			}	
	}
	public void setV(double x, double y){
		vX = x;
		vY = y;
		return;
	}
	
	public int getAge(){
		return(age);
	}
	
	public double getVX(){
		return(vX);
	}
	
	public double getVY(){
		return(vY);
	}
	
	public double getR() {
		return(radius);
	}

	public double getMass() {
		return(mass);
	}
	
	// returns one of {NO_COLLISION, START_COLLISION, IN_COLLISION}
	//  NO_COLLISION ==> this bonk doesnt overlap input bonk
	//  START_COLLISION ==> this bonk didn't overlap input bonk last time
	//   we checked, but it does now
	//  IN_COLLISION ==> this bonk overlaps input bonk, and it did the
	//   last time we checked
	public int collide(Sprite b){
		int state = Sprite.NO_COLLISION;
		double x = (centerX - b.centerX);
		double y = (centerY - b. centerY);
		double br = b.getR();
		double sqd = (x * x) + (y * y);  // distance between centers
		double sqc = (radius + br) * (radius + br);  // total distance of both radii
		// if they collide then...
		if((sqd) <= (sqc)){
			if(collisions.add(b)){
				state = Sprite.START_COLLISION;
				b.addCollision(this);
				tracers.add(new Tracer((int)((centerX*b.getR()+b.centerX*radius)/(radius+b.getR())),(int) ((centerY*b.getR()+b.centerY*radius)/(radius+b.getR())),(int)(mass+b.mass)/2, mTracerColor, mColor));
				
				
				// do the conservation-of-momentum thing
				double bvX = b.getVX();
				double bvY = b.getVY();
				double bmass = b.getMass();
				double tmpX0;
				double tmpY0;
				double tmpX1;
				double tmpY1;
				tmpX0 = ((vX * (mass - bmass)) + (2 * bmass * bvX)) / (mass + bmass);
				tmpY0 = ((vY * (mass - bmass)) + (2 * bmass * bvY)) / (mass + bmass);
				tmpX1 = ((bvX * (bmass - mass)) + (2 * mass * vX)) / (mass + bmass);
				tmpY1 = ((bvY * (bmass - mass)) + (2 * mass * vY)) / (mass + bmass);
				setV(tmpX0, tmpY0);
				b.setV(tmpX1, tmpY1);
			}
			else{
				state = Sprite.IN_COLLISION;
			}
		}
		else{
			state = Sprite.NO_COLLISION;
			collisions.remove(b);
			b.removeCollision(this);
		}
		return(state);
	}
	
	// returns true if this bonk hits the edge of the given rectangle
	public boolean collide(int w, int h){
		boolean hit = false;
		if((centerX + radius) >= w){
			centerX = w - radius;
			setV(-vX, vY);
			hit = true;
		}
		else if ((centerX - radius) <= 0){
			centerX = radius;
			setV(-vX, vY);
			hit = true;
		}
		if((centerY + radius) >= h){
			centerY = h - radius;
			setV(vX, -vY);
			hit = true;
		}
		else if((centerY - radius ) <= 0){
			centerY = radius;
			setV(vX, -vY);
			hit = true;
		}
		return(hit);
	}

	public void draw(Canvas c, Paint p){
		//int r = (int)radius;
		//if sprite
		if(mBitmap != null){
			if(radius*2!=mBitmap.getWidth()){
				scaleBitmap((int)radius*2);//
			}
			//Rotate?
			
				c.save();
	            c.rotate((float) mHeading, (float) centerX, (float) centerY);
				c.drawBitmap(mBitmap, (int)(centerX-(radius)), (int)(centerY-(radius)), p);
				c.restore();
				
			
		}
		/*
		if (age<10){
			p.setColor(mTracerColor);
			c.drawCircle((int)centerX, (int)centerY, (int)radius, p);
			 //draw depending on style
		}else if (age >200){
		
			p.setColor(colors[0]);
			c.drawCircle((int)centerX, (int)centerY, (int)radius, p);
		}else{
			p.setShader(new RadialGradient((int)centerX, (int)centerY, (int)radius, colors, pos, Shader.TileMode.CLAMP));
			p.setStyle(Paint.Style.FILL);
			p.setColor(colors[0]);
			c.drawCircle((int)centerX, (int)centerY, (int)radius, p);
			p.setStyle(Paint.Style.STROKE);
			p.setShader(null);

		}
*/
		
		return;
	}

	public void drawTracers(Canvas c, Paint p, int fcolor,int style){
		// pulse the radius
		if(tracers.size() > 0){
			switch(mTracerCounter){
			case 0:
				radius -= 2;
				mTracerCounter++;
				break;
			case 1:
				radius += 2;
				mTracerCounter = 0;
				break;
			}
		}
		if(tracers.size() > 0){
			float sw = p.getStrokeWidth();
			Paint.Style s = p.getStyle();
			int cp = p.getColor();
			for(Tracer fp : tracers){
				if(fp.draw(c, p, fcolor,style)){
					donetracers.add(fp);
				}
			}
			tracers.removeAll(donetracers);
			donetracers.clear();
			p.setStrokeWidth(sw);
			p.setColor(cp);
			p.setStyle(s);
		}
		return;
	}
	public boolean addCollision(Sprite b){
		return(collisions.add(b));
	}
	
	public boolean collidesWith(Sprite b){
		return(collisions.contains(b));
	}

	public boolean removeCollision(Sprite b){
		return(collisions.remove(b));
	}

	
	public double centerSqDistance(Sprite b){
		double dx = centerX - b.getCenterX();
		double dy = centerY - b.getCenterY();
		return((dx * dx) + (dy * dy));
	}
	
	public double getCenterY() {
		return(centerY);
	}
	
	public double getCenterX() {
		return(centerX);
	}
	
	public void update() {
		age++;
		centerX += vX;
		centerY += vY;
		return;
	}
}
	