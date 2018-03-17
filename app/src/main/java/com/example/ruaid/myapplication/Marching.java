package com.example.ruaid.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by ruaid on 18/02/2018.
 */

import android.graphics.DashPathEffect;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by ween on 12/14/14.
 */
public class Marching {

    public Paint paint;
    public Path path;
    public Canvas canvas;
    Queue<Point> q;

    private ArrayList<DashPathEffect> bees = new ArrayList<>();
    private int currentFrame = 0;

    public Marching(Bitmap bmp, int dashOn, int dashOff, float dp) {

        int width, height;
        int[] arrPixels;
        width = bmp.getWidth();
        height = bmp.getHeight();

        arrPixels = new int[width * height];

        bmp.getPixels(arrPixels, 0, width, 0, 0, width, height);


        Point n = q.poll();
        Paint fgPaintSel = new Paint();
        canvas.drawColor(Color.RED);
        //Point point = arrPixels[width * n.y + n.x];
        path.reset();
        path.moveTo(
                n.x,
                n.y);
//        path.lineTo(
//                (float)(x + innerRadius * Math.cos(0 + section/2.0)),
//                (float)(y + innerRadius * Math.sin(0 + section/2.0)));
//
//        for(int i=1; i<numOfPt; i++){
//            path.lineTo(
//                    (float)(x + radius * Math.cos(section * i)),
//                    (float)(y + radius * Math.sin(section * i)));
//            path.lineTo(
//                    (float)(x + innerRadius * Math.cos(section * i + section/2.0)),
//                    (float)(y + innerRadius * Math.sin(section * i + section/2.0)));
//        }

        path.close();

        canvas.drawPath(path, paint);
    }

    public DashPathEffect getNextPathEffect() {
        currentFrame = (currentFrame + 1) % bees.size();
        return bees.get(currentFrame);
    }



}
