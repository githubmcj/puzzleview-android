package com.dd.test.puzzleview_android.activity.entity.pointtest;

/**
 * @date: 2019/2/26 9:34
 * @author: Chunjiang Mao
 * @classname: Line
 * @describe:
 */
public class Line {
    private Point point_star;
    private Point point_end;

    public Point getPoint_star() {
        return point_star;
    }

    public void setPoint_star(Point point_star) {
        this.point_star = point_star;
    }

    public Point getPoint_end() {
        return point_end;
    }

    public void setPoint_end(Point point_end) {
        this.point_end = point_end;
    }

    public void setPoints(Point point_star, Point point_end) {
        this.point_star = point_star;
        this.point_end = point_end;
    }
}
