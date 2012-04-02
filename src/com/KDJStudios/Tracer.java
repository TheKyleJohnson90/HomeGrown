package com.KDJStudios;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class Tracer {
	
	private static final int	NFRAMES = 10;
	private static final Paint	p = new Paint();
	private RadialGradient	shader;
	private int				x;
	private int				y;
	private int				r;
	private int				count;
	
	static {
//		p.setMaskFilter(blur);
		p.setStyle(Paint.Style.STROKE);
	}
	
	Tracer(int xx, int yy, int rr, int c1, int c2){
		x = xx;
		y = yy;
		r = rr;
		count = 0;
		shader = new RadialGradient(x, y, r + 10, Color.argb(255, Color.red(c1), Color.green(c1), Color.blue(c1)), Color.argb(255, Color.red(c2), Color.green(c2), Color.blue(c2)), Shader.TileMode.CLAMP);
		return;
	}
	
	// returns true when we're done flashing.  the caller should
	//  stop calling us and delete us.
	public boolean draw(Canvas c, Paint pp, int fcolor,int style){
		count++;
		if(count == 1){//first hit small marker
			pp.setStyle(Paint.Style.FILL);
			pp.setShader(shader);
			c.drawCircle(x, y, 10, pp);
			pp.setStyle(Paint.Style.STROKE);
			pp.setShader(null);
		}else if (count>1){
			p.setColor(Color.argb(255 / (count>>1), Color.red(fcolor), Color.green(fcolor), Color.blue(fcolor)));
			p.setStrokeWidth(count * 4);
			if(style==2){//Explosion
				c.drawCircle(x, y, r * count, p);			
			}
			else{//Fade
	
				c.drawCircle(x, y, r - count, p);			
			}
		}

		return(count >= Tracer.NFRAMES);
	}
}
