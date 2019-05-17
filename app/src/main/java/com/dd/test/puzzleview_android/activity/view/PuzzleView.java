package com.dd.test.puzzleview_android.activity.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dd.test.puzzleview_android.activity.entity.Coordinates;
import com.dd.test.puzzleview_android.activity.entity.ImageBean;
import com.dd.test.puzzleview_android.activity.entity.ImageItem;
import com.dd.test.puzzleview_android.activity.entity.pointtest.JigsawStyle;
import com.dd.test.puzzleview_android.activity.entity.pointtest.Point;
import com.dd.test.puzzleview_android.activity.util.AppUtil;
import com.dd.test.puzzleview_android.activity.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dd on 16/1/13.
 * 拼图重要部分
 */
public class PuzzleView extends View {

    private Context context;
    private Path[] path;
    private Bitmap[] bitmaps;
    private boolean[] bitmapsFlag;
    private float[][] pathLT;
    private float[][] pathOffset;
    private int pathNum;
    private int viewWidth, viewHeight;
    private int lineWidth;

    private int leftMargin;
    private List<ImageBean> pics;
    private final static int MARGIN_HEIGHT = 100;
    private List<ImageItem> coordinateSetList;

    private int chosePath;
    private int changePath;

    private JigsawStyle jigsawStyle;

    private List<Point> allPoints = new ArrayList<>();
    private Bitmap[] repBitmaps;
    private boolean canEditFrame = false;

    public PuzzleView(Context context) {
        super(context);
        this.context = context;
    }

    public PuzzleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        initPath();
    }

    public PuzzleView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;
        initPath();
    }

    public void setPathCoordinate(List<ImageItem> pathCoordinate) {
        this.coordinateSetList = pathCoordinate;
        initPath();
    }

    /**
     * 设置点位置
     *
     * @param jigsawStyle
     */
    public void setPoint(JigsawStyle jigsawStyle) {
        this.jigsawStyle = jigsawStyle;
        initPath();
    }

    public void setPics(List<ImageBean> imageBeans) {
        lineWidth = 10;
        leftMargin = (AppUtil.getScreenWidth(context) - dp2px(320)) / 2;
        viewWidth = dp2px(320);
        viewHeight = dp2px(450);

        pics = new ArrayList<>();
        if (imageBeans != null) {
            pics.addAll(imageBeans);
        }
        pathNum = pics.size();

    }

    private void initPath() {
        // setPath
        path = new Path[pathNum];
        for (int i = 0; i < pathNum; i++) {
            path[i] = new Path();
        }
        bitmapsFlag = new boolean[pathNum];

        pathLT = new float[pathNum][2];
        pathOffset = new float[pathNum][2];
        for (int i = 0; i < pathNum; i++) {
            bitmapsFlag[i] = false;
            pathLT[i][0] = 0f;
            pathLT[i][1] = 0f;
            pathOffset[i][0] = 0f;
            pathOffset[i][1] = 0f;
        }

        initPoints();

        setPath();

        // get bitmap
        bitmaps = new Bitmap[pathNum];
        repBitmaps = new Bitmap[pathNum];
        for (int i = 0; i < pathNum; i++) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pics.get(i).path, opt);

            int bmpWdh = opt.outWidth;
            int bmpHgt = opt.outHeight;

            Point point = caculateViewSize(jigsawStyle.getImages().get(i).getPoints());
            int size = caculateSampleSize(bmpWdh, bmpHgt, dp2px(point.getX()), dp2px(point.getY()));
            opt.inJustDecodeBounds = false;
            opt.inSampleSize = size;
            bitmaps[i] = scaleImage(BitmapFactory.decodeFile(pics.get(i).path, opt), dp2px(point.getX()), dp2px(point.getY()));
            repBitmaps[i] = scaleImage(BitmapFactory.decodeFile(pics.get(i).path), dp2px(point.getX()), dp2px(point.getY()));
        }
    }

    private void setPath() {
        for (int i = 0; i < pathNum; i++) {
            path[i].reset();
        }
        for (int i = 0; i < pathNum; i++) {
            for (int j = 0; j < jigsawStyle.getImages().get(i).getPoints().size(); j++) {
                if (j == 0) {
                    path[i].moveTo(dp2px(jigsawStyle.getImages().get(i).getPoints().get(j).getX()), dp2px(jigsawStyle.getImages().get(i).getPoints().get(j).getY()));
                } else {
                    path[i].lineTo(dp2px(jigsawStyle.getImages().get(i).getPoints().get(j).getX()), dp2px(jigsawStyle.getImages().get(i).getPoints().get(j).getY()));
                }
            }
            // 如果连接Path起点和终点能形成一个闭合图形，则会将起点和终点连接起来形成一个闭合图形
            path[i].close();
        }
    }

    /**
     * 初始化点的位置，根据模板确定图片位置
     */
    private void initPoints() {
        if (jigsawStyle != null) {
            allPoints = jigsawStyle.getAllPoints();
            for (int i = 0; i < jigsawStyle.getImages().size(); i++) {
                List<Point> points = new ArrayList<>();
                for (int j = 0; j < jigsawStyle.getImages().get(i).getPointIndex().size(); j++) {
                    points.add(allPoints.get(jigsawStyle.getImages().get(i).getPointIndex().get(j)));
                }
                jigsawStyle.getImages().get(i).setPoints(points);
            }
        }
    }

    private Point caculateViewSize(List<Point> list) {
        float viewWidth;
        float viewHeight;
        viewWidth = caculateMaxPointX(list) - caculateMinPointX(list);
        viewHeight = caculateMaxPointY(list) - caculateMinPointY(list);
        return new Point(viewWidth, viewHeight);
    }

//    private Coordinates caculateViewSize(List<Coordinates> list) {
//
//        float viewWidth;
//        float viewHeight;
//
//        viewWidth = caculateMaxCoordinateX(list) - caculateMinCoordinateX(list);
//        viewHeight = caculateMaxCoordinateY(list) - caculateMinCoordinateY(list);
//
//        return new Coordinates(viewWidth, viewHeight);
//    }

    private int caculateSampleSize(int picWdh, int picHgt, int showWdh,
                                   int showHgt) {
        // 如果此时显示区域比图片大，直接返回
        if ((showWdh < picWdh) && (showHgt < picHgt)) {
            int wdhSample = picWdh / showWdh;
            int hgtSample = picHgt / showHgt;
            // 利用小的来处理
            int sample = wdhSample > hgtSample ? hgtSample : wdhSample;
            int minSample = 2;
            while (sample > minSample) {
                minSample *= 2;
            }
            return minSample >> 1;
        } else {
            return 0;
        }
    }

    private float caculateMinPointY(List<Point> points) {
        float minY;
        minY = points.get(0).getY();
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).getY() < minY) {
                minY = points.get(i).getY();
            }
        }
        return minY;
    }

    private float caculateMinPointX(List<Point> points) {
        float minX;
        minX = points.get(0).getX();
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).getX() < minX) {
                minX = points.get(i).getX();
            }
        }
        return minX;
    }

    private float caculateMaxPointY(List<Point> points) {
        float maxY;
        maxY = points.get(0).getY();
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).getY() > maxY) {
                maxY = points.get(i).getY();
            }
        }
        return maxY;
    }

    private float caculateMaxPointX(List<Point> points) {
        float maxX;
        maxX = points.get(0).getX();
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).getX() > maxX) {
                maxX = points.get(i).getX();
            }
        }
        return maxX;
    }

    private float caculateMinCoordinateX(List<Coordinates> list) {

        float minX;
        minX = list.get(0).getX();
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).getX() < minX) {
                minX = list.get(i).getX();
            }
        }
        return minX;
    }

    private float caculateMaxCoordinateX(List<Coordinates> list) {

        float maxX;
        maxX = list.get(0).getX();
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).getX() > maxX) {
                maxX = list.get(i).getX();
            }
        }
        return maxX;
    }

    private float caculateMinCoordinateY(List<Coordinates> list) {

        float minY;
        minY = list.get(0).getY();
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).getY() < minY) {
                minY = list.get(i).getY();
            }
        }
        return minY;
    }

    private float caculateMaxCoordinateY(List<Coordinates> list) {

        float maxY;
        maxY = list.get(0).getY();
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).getY() > maxY) {
                maxY = list.get(i).getY();
            }
        }
        return maxY;
    }

    //图片缩放
    private static Bitmap scaleImage(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null) {
            return null;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        float scale = 1;
        if (scaleWidth >= scaleHeight) {
            scale = scaleWidth;
        } else {
            scale = scaleHeight;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        return newbm;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(viewWidth, viewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 显示背景颜色
        canvas.drawColor(Color.GREEN);

        startDraw(canvas);
        if (canEditFrame) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(6);
            drawableFrame(canvas, paint);
            Paint paint1 = new Paint();
            paint1.setAntiAlias(true);
            paint1.setColor(Color.RED);
            paint1.setStrokeWidth(30);
            drawablePoint(canvas, paint1);
        } else {

        }
    }

    private void drawablePoint(Canvas canvas, Paint paint) {
        for (int i = 0; i < allPoints.size(); i++) {
            canvas.drawPoint(dp2px(allPoints.get(i).getX()), dp2px(allPoints.get(i).getY()), paint);
        }
    }

    /**
     * 绘制框架
     *
     * @param canvas
     * @param paint
     */
    private void drawableFrame(Canvas canvas, Paint paint) {
        for (int i = 0; i < jigsawStyle.getImages().size(); i++) {
            for (int j = 0; j < jigsawStyle.getImages().get(i).getPoints().size(); j++) {
                if (j != jigsawStyle.getImages().get(i).getPoints().size() - 1) {
                    canvas.drawLine(dp2px(jigsawStyle.getImages().get(i).getPoints().get(j).getX()), dp2px(jigsawStyle.getImages().get(i).getPoints().get(j).getY()), dp2px(jigsawStyle.getImages().get(i).getPoints().get(j + 1).getX()), dp2px(jigsawStyle.getImages().get(i).getPoints().get(j + 1).getY()), paint);
                } else {
                    canvas.drawLine(dp2px(jigsawStyle.getImages().get(i).getPoints().get(j).getX()), dp2px(jigsawStyle.getImages().get(i).getPoints().get(j).getY()), dp2px(jigsawStyle.getImages().get(i).getPoints().get(0).getX()), dp2px(jigsawStyle.getImages().get(i).getPoints().get(0).getY()), paint);
                }
            }
        }

    }

    private void startDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < pathNum; i++) {
            // 用来保存Canvas的状态,save()方法之后的代码，能够调用Canvas的平移、放缩、旋转、裁剪等操作
            canvas.save();
            drawScene(canvas, paint, i);
            // 用来恢复Canvas之前保存的状态,防止save()方法代码之后对Canvas运行的操作。继续对兴许的绘制会产生影响。通过该方法能够避免连带的影响！
            canvas.restore();
        }
    }

    private void drawScene(Canvas canvas, Paint paint, int idx) {
        canvas.clipPath(path[idx]);
        canvas.drawPath(path[idx], paint);
        bitmaps[idx] = scaleImage(repBitmaps[idx], dp2px(caculateMaxPointX(jigsawStyle.getImages().get(idx).getPoints())) - dp2px(caculateMinPointX(jigsawStyle.getImages().get(idx).getPoints())), dp2px(caculateMaxPointY(jigsawStyle.getImages().get(idx).getPoints())) - dp2px(caculateMinPointY(jigsawStyle.getImages().get(idx).getPoints())));
        if (bitmapsFlag[idx]) {
            canvas.drawBitmap(bitmaps[idx], dp2px(caculateMinPointX(jigsawStyle.getImages().get(idx).getPoints())) + pathOffsetX + pathOffset[idx][0],
                    dp2px(caculateMinPointY(jigsawStyle.getImages().get(idx).getPoints())) + pathOffsetY + pathOffset[idx][1], paint);
        } else {
            canvas.drawBitmap(bitmaps[idx], dp2px(caculateMinPointX(jigsawStyle.getImages().get(idx).getPoints())) + pathOffset[idx][0],
                    dp2px(caculateMinPointY(jigsawStyle.getImages().get(idx).getPoints())) + pathOffset[idx][1], paint);
        }

    }

    private int dp2px(float point) {
        return DensityUtil.dip2px(getContext(), point);
    }

    /**
     * 将像素转换成dp
     *
     * @param pxValue
     * @return
     */
    public int px2dip(float pxValue) {
        return (int) (pxValue / getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    float ptx, pty;
    float pathOffsetX, pathOffsetY;
    float pointx, pointy;
    private int potintFlag = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("----", event.getX() + "-------------" + event.getY());
        Log.e("----viewWidth-----", viewWidth + "-------------");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (canEditFrame) {
                    for (int i = 0; i < allPoints.size(); i++) {
                        Log.e("----", Math.pow((px2dip(event.getX()) - allPoints.get(i).getX()), 2) + Math.pow((px2dip(event.getY()) - allPoints.get(i).getY()), 2) + "------dsd-------");
                        if (Math.pow((px2dip(event.getX()) - allPoints.get(i).getX()), 2) + Math.pow((px2dip(event.getY()) - allPoints.get(i).getY()), 2) < 600) {
                            allPoints.get(i).setMove(true);
                            potintFlag = i;
                        }
                    }
                    if (potintFlag != -1) {
                        if (event.getX() < 0) {
                            pointx = 0;
                        } else if (event.getX() > viewWidth) {
                            pointx = px2dip(viewWidth);
                        } else {
                            pointx = px2dip(event.getX());
                        }
                        if (event.getY() < 0) {
                            pointy = 0;
                        } else if (event.getY() > viewHeight) {
                            pointy = px2dip(viewHeight);
                        } else {
                            pointy = px2dip(event.getY());
                        }
                        allPoints.get(potintFlag).setX(pointx);
                        allPoints.get(potintFlag).setY(pointy);
                    }
                    setPath();
                } else {
                    for (int i = 0; i < pathNum; i++) {
                        bitmapsFlag[i] = false;
                    }
                    ptx = event.getRawX() - dp2px(leftMargin);
                    pty = event.getRawY() - dp2px(MARGIN_HEIGHT);
                    pathOffsetX = 0;
                    pathOffsetY = 0;
                    for (int cflag = 0; cflag < pathNum; cflag++) {
                        if (contains(path[cflag], ptx, pty)) {
                            chosePath = cflag;
                            bitmapsFlag[cflag] = true;
                            break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (canEditFrame) {
                    if (potintFlag != -1) {
                        if (event.getX() < 0) {
                            pointx = 0;
                        } else if (event.getX() > viewWidth) {
                            pointx = px2dip(viewWidth);
                        } else {
                            pointx = px2dip(event.getX());
                        }
                        if (event.getY() < 0) {
                            pointy = 0;
                        } else if (event.getY() > viewHeight) {
                            pointy = px2dip(viewHeight);
                        } else {
                            pointy = px2dip(event.getY());
                        }
                        allPoints.get(potintFlag).setX(pointx);
                        allPoints.get(potintFlag).setY(pointy);
                    }
                    setPath();
                    invalidate();
                } else {
                    pathOffsetX = event.getRawX() - dp2px(leftMargin) - ptx;
                    pathOffsetY = event.getRawY() - dp2px(MARGIN_HEIGHT) - pty;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                if (canEditFrame) {
                    if (potintFlag != -1) {
                        if (event.getX() < 0) {
                            pointx = 0;
                        } else if (event.getX() > viewWidth) {
                            pointx = px2dip(viewWidth);
                        } else {
                            pointx = px2dip(event.getX());
                        }
                        if (event.getY() < 0) {
                            pointy = 0;
                        } else if (event.getY() > viewHeight) {
                            pointy = px2dip(viewHeight);
                        } else {
                            pointy = px2dip(event.getY());
                        }
                        allPoints.get(potintFlag).setX(pointx);
                        allPoints.get(potintFlag).setY(pointy);
                    }
                    for (int i = 0; i < allPoints.size(); i++) {
                        allPoints.get(i).setMove(false);
                        potintFlag = -1;
                    }
                    setPath();
                    invalidate();
                } else {
                    // 修改图片位置
                    for (int cflag = 0; cflag < pathNum; cflag++) {
                        if (contains(path[cflag], event.getRawX() - dp2px(leftMargin), event.getRawY() - dp2px(MARGIN_HEIGHT))) {
                            changePath = cflag;
                            break;
                        }
                    }
                    if (chosePath != changePath) {
                        Bitmap re_bitmap = bitmaps[chosePath];
                        bitmaps[chosePath] = bitmaps[changePath];
                        bitmaps[changePath] = re_bitmap;
                    }

                    for (int i = 0; i < pathNum; i++) {
                        if (bitmapsFlag[i]) {
                            pathOffset[i][0] += event.getRawX() - dp2px(leftMargin) - ptx;
                            pathOffset[i][1] += event.getRawY() - dp2px(MARGIN_HEIGHT) - pty;

                            if (pathOffset[i][0] > 0) {
                                pathOffset[i][0] = 0;
                            }
                            if (pathOffset[i][0] < -(bitmaps[i].getWidth() - getViewWidth(coordinateSetList.get(i).getCoordinates()))) {
                                pathOffset[i][0] = -(bitmaps[i].getWidth() - getViewWidth(coordinateSetList.get(i).getCoordinates()));
                            }
                            if (pathOffset[i][1] > 0) {
                                pathOffset[i][1] = 0;
                            }
                            if (pathOffset[i][1] < -(bitmaps[i].getHeight() - getViewHeight(coordinateSetList.get(i).getCoordinates()))) {
                                pathOffset[i][1] = -(bitmaps[i].getHeight() - getViewHeight(coordinateSetList.get(i).getCoordinates()));
                            }
                            bitmapsFlag[i] = false;
                            break;
                        }
                    }
                }

                break;
            default:
                break;
        }

        return true;
    }

    private boolean contains(Path parapath, float pointx, float pointy) {
        RectF localRectF = new RectF();
        parapath.computeBounds(localRectF, true);
        Region localRegion = new Region();
        localRegion.setPath(parapath, new Region((int) localRectF.left,
                (int) localRectF.top, (int) localRectF.right,
                (int) localRectF.bottom));
        return localRegion.contains((int) pointx, (int) pointy);
    }

    private float getViewWidth(List<Coordinates> list) {

        return dp2px(caculateMaxCoordinateX(list) - caculateMinCoordinateX(list));
    }

    private float getViewHeight(List<Coordinates> list) {

        return dp2px(caculateMaxCoordinateY(list) - caculateMinCoordinateY(list));
    }

    public void setCanEdit(boolean canEditFrame) {
        this.canEditFrame = canEditFrame;
        invalidate();
    }
}
