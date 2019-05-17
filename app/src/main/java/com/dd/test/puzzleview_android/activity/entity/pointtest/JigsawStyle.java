package com.dd.test.puzzleview_android.activity.entity.pointtest;

import java.util.List;

/**
 * @date: 2019/2/26 9:36
 * @author: Chunjiang Mao
 * @classname: JigsawStyle
 * @describe:
 */
public class JigsawStyle {
    private List<Image> images;
    private List<Point> allPoints;

    public List<Point> getAllPoints() {
        return allPoints;
    }

    public void setAllPoints(List<Point> allPoints) {
        this.allPoints = allPoints;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
}
