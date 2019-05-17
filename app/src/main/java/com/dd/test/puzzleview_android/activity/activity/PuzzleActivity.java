package com.dd.test.puzzleview_android.activity.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dd.test.puzzleview_android.R;
import com.dd.test.puzzleview_android.activity.dialog.TemplateDialog;
import com.dd.test.puzzleview_android.activity.entity.ImageBean;
import com.dd.test.puzzleview_android.activity.entity.Puzzle;
import com.dd.test.puzzleview_android.activity.entity.pointtest.Jigsaw;
import com.dd.test.puzzleview_android.activity.util.FileUtil;
import com.dd.test.puzzleview_android.activity.view.PuzzleView;
import com.dd.test.puzzleview_android.activity.view.TopView;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by dd on 16/1/13.
 * 拼图主界面
 */
public class PuzzleActivity extends Activity implements View.OnClickListener {

    private Context context;
    private TopView topView;
    private LinearLayout puzzleLL;
    private PuzzleView puzzleView;
    private TextView templateTv;
    private List<ImageBean> imageBeans;
    private Puzzle puzzleEntity;

    /**
     * 拼图模板
     */
    private Jigsaw jigsaw;
    private TemplateDialog templateDialog;
    private String pathFileName;
    private int lastSelect = 0;
    private boolean canEditFrame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        init();
    }

    private void init() {

        context = PuzzleActivity.this;
        initView();
        initData();
        initEvent();

    }

    private void initView() {

        topView = (TopView) findViewById(R.id.top_view);
        puzzleLL = (LinearLayout) findViewById(R.id.puzzle_ll);
        puzzleView = (PuzzleView) findViewById(R.id.puzzle_view);
        templateTv = (TextView) findViewById(R.id.template_tv);


    }

    private void initData() {

        imageBeans = (List<ImageBean>) getIntent().getSerializableExtra("pics");
        getFileName(imageBeans.size());
        templateDialog = new TemplateDialog(context, imageBeans.size());
        topView.setTitle("拼图");
        topView.setRightWord("保存");
        puzzleView.setPics(imageBeans);
        if (pathFileName != null) {
            initPointData(pathFileName, 0);
        }
    }

    private void initEvent() {
        templateTv.setOnClickListener(this);

        topView.setOnLeftClickListener(new TopView.OnLeftClickListener() {
            @Override
            public void leftClick() {
                finish();
            }
        });
        topView.setOnRightClickListener(new TopView.OnRightClickListener() {
            @Override
            public void rightClick() {
                savePuzzle();
                finish();
            }
        });

        templateDialog.setOnItemClickListener(new TemplateDialog.OnItemClickListener() {
            @Override
            public void OnItemListener(int position) {
                if (position != lastSelect) {
                    initPointData(pathFileName, position);
                    puzzleView.invalidate();
                    lastSelect = position;
                }
                templateDialog.dismiss();
            }
        });


        topView.setRightWord("编辑");
        topView.setOnRightClickListener(new TopView.OnRightClickListener() {
            @Override
            public void rightClick() {
                canEditFrame = !canEditFrame;
                puzzleView.setCanEdit(canEditFrame);
            }
        });

    }

    private void getFileName(int picNum) {

        switch (picNum) {

            case 2:
                pathFileName = "num_two_jigsaw_style";
                break;
            case 3:
                pathFileName = "num_three_jigsaw_style";
                break;
//            case 4:
//                pathFileName = "num_four_style";
//                break;
//            case 5:
//                pathFileName = "num_five_style";
//                break;
            default:
                break;
        }
    }

    /**
     * 获取模板数据，设置点的位置
     * @param fileName
     * @param templateNum
     */
    private void initPointData(String fileName, int templateNum) {

        String data = new FileUtil(context).readAsset(fileName);
        try {
            Gson gson = new Gson();
            jigsaw = gson.fromJson(data, Jigsaw.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jigsaw != null && jigsaw.getStyle() != null && jigsaw.getStyle().get(templateNum).getAllPoints() != null  && jigsaw.getStyle().get(templateNum).getAllPoints().size() > 0 && jigsaw.getStyle().get(templateNum).getImages() != null  && jigsaw.getStyle().get(templateNum).getImages().size() > 0) {
           // 选择的模板，包含坐标点位置
            puzzleView.setPoint(jigsaw.getStyle().get(templateNum));
        }

    }

    private void savePuzzle() {

        buildDrawingCache(puzzleLL);
        puzzleLL.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        Bitmap bitmap = puzzleLL.getDrawingCache().copy(Bitmap.Config.RGB_565, true);
        try {
            File file = FileUtil.saveBitmapJPG(context,"dd" + System.currentTimeMillis(), bitmap);
            Intent intent = new Intent("puzzle");
            intent.putExtra("picPath", file.getPath());
            sendBroadcast(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildDrawingCache(View view) {
        try {
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.template_tv:
                templateDialog.show();
                break;

            default:
                break;
        }
    }

}
