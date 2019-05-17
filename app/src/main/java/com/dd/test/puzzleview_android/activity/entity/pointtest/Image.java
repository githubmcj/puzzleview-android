package com.dd.test.puzzleview_android.activity.entity.pointtest;

import java.util.List;

/**
 * @date: 2019/2/26 9:35
 * @author: Chunjiang Mao
 * @classname: Image
 * @describe:
 */
public class Image {

    /**
     * 图形点的顺序
     */
    private List<Integer> pointIndex;

    /**
     * 通过点的顺序获取点
     */
    private List<Point> points;

    public List<Integer> getPointIndex() {
        return pointIndex;
    }

    public void setPointIndex(List<Integer> pointIndex) {
        this.pointIndex = pointIndex;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

}
