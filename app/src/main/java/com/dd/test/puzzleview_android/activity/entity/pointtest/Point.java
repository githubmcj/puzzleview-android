package com.dd.test.puzzleview_android.activity.entity.pointtest;

/**
 * @date: 2019/2/26 9:33
 * @author: Chunjiang Mao
 * @classname: Point
 * @describe:
 */
public class Point {
    private boolean move;
    private float x;
    private float y;

    public Point(float viewWidth, float viewHeight) {
        this.x = viewWidth;
        this.y = viewHeight;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
