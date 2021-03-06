package com.jagnat.blindsgf;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.util.*;
import android.content.Context;

import com.jagnat.golib.GameNode;

public class GobanView extends View {

    private Paint gridPaint = new Paint();
    private Paint starPaint = new Paint();
    private Paint whiteStonePaint = new Paint();
    private Paint blackStonePaint = new Paint();

    private int lastMoveX = -1;
    private int lastMoveY = -1;
    private GameNode.Color lastMoveColor = GameNode.Color.BLACK;

    private float stoneSize = 0;

    private final int BOARD_SIZE = 19;

    public GobanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint.setColor(0xffd4af37);
        gridPaint.setStrokeWidth(2.0f);

        starPaint.setColor(0xffd4af37);
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setAntiAlias(true);

        whiteStonePaint.setColor(Color.WHITE);
        whiteStonePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float boardSize = (float)View.MeasureSpec.getSize(getMeasuredWidth());

        for (int i = 0; i < BOARD_SIZE; i++) {
            canvas.drawLine(i * stoneSize + stoneSize / 2.0f, stoneSize / 2.0f, i * stoneSize + stoneSize / 2.0f, boardSize - stoneSize / 2.0f, gridPaint);
            canvas.drawLine(stoneSize / 2.0f,i * stoneSize + stoneSize / 2.0f, boardSize - stoneSize / 2.0f,i * stoneSize + stoneSize / 2.0f, gridPaint);
        }

        // Draw hoshi
        float offset = 3.5f * stoneSize;
        float inter_dist = 6.f * stoneSize;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                canvas.drawCircle(offset + i * inter_dist,  offset + j * inter_dist,stoneSize / 8.f, starPaint);
            }
        }

        if (lastMoveX != -1 && lastMoveY != -1) {
            Paint stonePaint = lastMoveColor == GameNode.Color.BLACK ? blackStonePaint : whiteStonePaint;
            canvas.drawCircle((lastMoveX - 1) * stoneSize + stoneSize / 2.0f, (lastMoveY - 1) * stoneSize + stoneSize / 2.0f, stoneSize/2, stonePaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = View.MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(width, width);

        stoneSize = width / (float)BOARD_SIZE;
    }

    public void setLastMove(int x, int y, GameNode.Color color) {
        lastMoveX = x;
        lastMoveY = y;
        lastMoveColor = color;
    }
}
