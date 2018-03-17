package com.example.ruaid.myapplication;

/**
 * Created by ruaid on 07/12/2017.
 */

import android.graphics.Bitmap;
import android.graphics.Point;
import java.util.LinkedList;
import java.util.Queue;

public class FloodFill extends MainActivity {

    int minR, maxR, minG, maxG, minB, maxB;  // instance values

    public Queue<Point> floodFill_array(Bitmap bmp, Point pt, int targetColor, int replacementColor, int tolerance) {
        if (targetColor == replacementColor)
            return null;

        /* tolerable values */
        minR = ((targetColor & 0xFF0000) >> 16) - tolerance;
        if (minR < 0) minR = 0;
        else minR = minR << 16;
        maxR = ((targetColor & 0xFF0000) >> 16) + tolerance;
        if (maxR > 0xFF) maxR = 0xFF0000;
        else maxR = maxR << 16;

        minG = ((targetColor & 0x00FF00) >> 8) - tolerance;
        if (minG < 0) minG = 0;
        else minG = minG << 8;
        maxG = ((targetColor & 0x00FF00) >> 8) + tolerance;
        if (maxG > 0xFF) maxG = 0x00FF00;
        else maxG = maxG << 8;

        minB = (targetColor & 0x0000FF) - tolerance;
        if (minB < 0) minB = 0;
        maxB = (targetColor & 0x0000FF) + tolerance;
        if (maxB > 0xFF) maxB = 0x0000FF;
        /* tolerable values */

        int width, height;
        int[] arrPixels;
        int[] arrPixelResult;

        width = bmp.getWidth();
        height = bmp.getHeight();


        arrPixels = new int[width * height];
        arrPixelResult= new int[width * height];
        bmp.getPixels(arrPixels, 0, width, 0, 0, width, height);
        bmp.getPixels(arrPixelResult, 0, width, 0, 0, width, height);


        Queue<Point> q = new LinkedList<Point>();
        Queue<Point> march = new LinkedList<Point>();
        q.add(pt);


        while (q.size() > 0) {

            Point n = q.poll(); // Retrieves and removes the head of this queue, or returns null if this queue is empty

            if (!isTolerable(arrPixels[width * n.y + n.x]))
                continue;

            Point w = n, e = new Point(n.x + 1, n.y); // w = west, e = west + 1 right
            while ((w.x > 0) && isTolerable(arrPixels[width * w.y + w.x])) {            // go west while the pixel to the west is tolerable
                arrPixels[width * w.y + w.x] = replacementColor;  // setPixel           // add pixel to the the array of pixels to be changed

                if ((w.y > 0) && isTolerable(arrPixels[width * (w.y - 1) + w.x]))       // check up after going west
                    q.add(new Point(w.x, w.y - 1));

                if ((w.y < height - 1) && isTolerable(arrPixels[width * (w.y + 1) + w.x]))  // check down
                    q.add(new Point(w.x, w.y + 1));

                w.x--;
            }

            arrPixelResult[width * w.y + w.x] = 0xffffff00;  // this pixel to be stored is the edge of the group. ie the outline

            while ((e.x < width - 1) && isTolerable(arrPixels[width * e.y + e.x])) {    // Now the same for East
                arrPixels[width * e.y + e.x] = replacementColor;  // setPixel

                if ((e.y > 0) && isTolerable(arrPixels[width * (e.y - 1) + e.x]))
                    q.add(new Point(e.x, e.y - 1));

                if ((e.y < height - 1) && isTolerable(arrPixels[width * (e.y + 1) + e.x]))
                    q.add(new Point(e.x, e.y + 1));

                e.x++;
            }

            arrPixelResult[width * e.y + e.x] = 0xffffff00;
        }

        bmp.setPixels(arrPixelResult, 0, width, 0, 0, width, height);

        return march;
    }


    /**
     * If the passed color is tolerable, return true.
     */
    private boolean isTolerable(int currentColor) {
        int r = currentColor & 0xFF0000;
        int g = currentColor & 0x00FF00;
        int b = currentColor & 0x0000FF;

        if (r < minR || r > maxR || g < minG || g > maxG || b < minB || b > maxB)
            return false;   // less than or grater than tolerable values
        else
            return true;
    }
}