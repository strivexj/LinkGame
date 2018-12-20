package com.strivexj.linkgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cwj on 12/19/18 22:46
 */
public class DrawView extends View {
    private Paint paint;
    private List<Point> points = new ArrayList<>();
    private Timer timer = new Timer();

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public void drawLine(List<Point> points) {
        timer.cancel();
        timer.purge();
        timer = new Timer();
        this.points = points;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            if (i % 2 != 0) {
                canvas.drawLine(p1.x-5, p1.y+5, p2.x-5, p2.y+5, paint);
            } else {
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                points = new ArrayList<>();
                invalidate();
            }
        }, 500);
    }
}

