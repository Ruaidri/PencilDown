package com.example.ruaid.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.Queue;

/**
 * Created by ruaid on 07/12/2017.
 */

public class DashedLineView extends View {

    private Paint   mPaint;
    private Path    mPath;
    private int     vWidth;
    private int     vHeight;
    public boolean touched = false;

    Canvas canvas = new Canvas();

    Queue<Point> q;

    public DashedLineView(Context context) {
        super(context);
        Log.d("Got here ", "CALLED FROM TOUCH");

        init();
    }

    public DashedLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DashedLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#e9ff02"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(32);
        mPaint.setPathEffect(new DashPathEffect(new float[] {10,10}, 0));
        mPath = new Path();
        touched = MainActivity.touched;
        if(touched == true){
            drawOutline();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        this.vWidth = getMeasuredWidth();
        this.vHeight = getMeasuredHeight();
        mPath.moveTo(50, this.vHeight / 2);
        mPath.quadTo(this.vWidth / 2, this.vHeight / 2, this.vWidth, this.vHeight / 2);


//            mPath.lineTo(
//                    (float)(x + radius * Math.sin(section * i))),
//                    (float)(y + radius * Math.sin(section * i)));
//            mPath.lineTo(
//                    (float)(x + innerRadius * Math.cos(section * i + section/2.0)),
//                    (float)(y + innerRadius * Math.sin(section * i + section/2.0)));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawPath(mPath, mPaint);
        canvas.drawText("Add Location",50,500,mPaint);
//        Paint BluePaint=new Paint();
//
//        BluePaint.setColor(Color.BLUE);
//
//        BluePaint.setStrokeWidth(1.5f);
//
//
        //canvas.restore();

    }



    public void drawOutline(){
        q =MainActivity.q;
//        if(q!=null) {
//            Log.d("Got here ", String.valueOf(q.size()));
//            //Traverse queue
//            Iterator it = q.iterator();
//            Point n = q.poll();
//            Point check = new Point();
//            mPath.reset();
//            while (q.size()>0) {
//                int iteratorValue = n.x;
//                Log.d("Iterator Val: ", String.valueOf(n.x));
//                n.x++;
//                mPath.lineTo(
//                        (float) (n.x),
//                        (float) (n.y)
//                );
//                n.x++;
//                n.y++;
//            }
//            mPath.close();
//            canvas.drawPath(mPath, mPaint);
//        }
    }
}