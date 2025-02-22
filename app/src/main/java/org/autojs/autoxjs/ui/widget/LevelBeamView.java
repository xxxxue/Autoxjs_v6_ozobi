package org.autojs.autoxjs.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.stardust.view.accessibility.NodeInfo;

import org.autojs.autoxjs.R;
import org.autojs.autoxjs.ui.floating.layoutinspector.LayoutHierarchyFloatyWindow;

/**
 * Created by Stardust on 2017/3/10.
 */

public class LevelBeamView extends View {

    private static final String TAG = "LevelBeamView";

    private static final int[] colors = {
            0xff1abc9c,
            0xff3498db,
            0xffe67e22,
            0xff8e44ad,
            0xfff1c40f,
            0xff2ecc71,
    };

    private int mLevel;

    private int mPaddingLeft, mPaddingRight;
    private int mLinesWidth;
    private int mLinesOffset;
    private Paint mLinePaint;
    // Added by Ozobi - 2024/11/02 >
    private Paint mLevelTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private Boolean mClickable;
    private Boolean mHasDesc;
    private Boolean mHasText;
    public static int levelInfoTextColor = Color.BLACK;
    public static NodeInfo selectedNode = null;
    private Paint selectedPaint;
    private NodeInfo curNodeInfo;
    // <

    public LevelBeamView(Context context) {
        super(context);
        init();
    }

    public LevelBeamView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LevelBeamView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setLevel(int level) {
        mLevel = level;
        requestLayout();
    }
    // Added by Ozobi - 2024/11/02 >
    public void setNodeInfo(Boolean clickable,Boolean hasDesc,Boolean hasText){
        mClickable = clickable;
        mHasDesc = hasDesc;
        mHasText = hasText;
        requestLayout();
    }
    public void setCurNodeInfo(NodeInfo nodeInfo){
        curNodeInfo = nodeInfo;
        if(curNodeInfo == selectedNode){
            if(levelInfoTextColor == Color.BLACK){
                selectedPaint.setColor(0xff7DFF6B);
            }else{
                selectedPaint.setColor(0xff008F5D);
            }
        }else{
            selectedPaint.setColor(0);
        }
        requestLayout();
    }
    // <
    private void init() {
        setWillNotDraw(false);
        mPaddingLeft = (int) getResources().getDimension(R.dimen.level_beam_view_padding_left);
        mPaddingRight = (int) getResources().getDimension(R.dimen.level_beam_view_padding_right);

        mLinesWidth = (int) getResources().getDimension(R.dimen.level_beam_view_line_width);
        mLinesOffset = (int) getResources().getDimension(R.dimen.level_beam_view_line_offset);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(mLinesWidth);
        // Added by Ozobi - 2024/11/02 >
        mLevelTextPaint = new Paint();
        Rect textBounds = new Rect();
        mLevelTextPaint.setTextSize(32f);
        mLevelTextPaint.setColor(levelInfoTextColor);
        mLevelTextPaint.getTextBounds("<000>",0,"<000>".length(), textBounds);
        mLevelTextPaint.setTextAlign(Paint.Align.LEFT);
        selectedPaint = new Paint();
        selectedPaint.setStyle(Paint.Style.FILL);
        mTextWidth = (float) textBounds.width();
        mTextHeight = (float) textBounds.height();
        mClickable = false;
        mHasText = false;
        mHasDesc = false;
        // <
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = mPaddingLeft + mPaddingRight + mLevel * (mLinesWidth + mLinesOffset) + (int) mTextWidth;// Modified by Ozobi - 2024/11/02 > added textWidth, 将线条数量改为与 level 相同: -1
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // Added by Ozobi - 2024/11/02 >
        canvas.drawRect(0, 0, mTextWidth, getHeight(), selectedPaint);
        String levelText = mLevel+">";
        canvas.drawText(levelText,16, mTextHeight +6f, mLevelTextPaint);
        String infoText = "";
        if(mClickable){
            infoText += "c ";
        }else{
            infoText += "- ";
        }
        if(mHasDesc){
            infoText +="d ";
        }else{
            infoText += "- ";
        }
        if(mHasText){
            infoText += "t ";
        }else{
            infoText += "- ";
        }
        canvas.drawText(infoText,16, (mTextHeight +9f)*2, mLevelTextPaint);
        // <
         for (int lvl = 0; lvl < mLevel; lvl++) {// Modified by Ozobi - 2025/01/30 > 将线条数量改为与 level 相同
            float LINE_X = mTextWidth + mPaddingLeft + lvl * mLinesWidth;// Modified by Ozobi - 2024/11/02 > added textWidth
            if (lvl >= 1) {
                LINE_X += lvl * mLinesOffset;
            }
            mLinePaint.setColor(getColorForLevel(lvl));
            canvas.drawLine(LINE_X, 0, LINE_X, getHeight(), mLinePaint);
        }
    }

    private int getColorForLevel(int level) {
        return colors[level % colors.length];
    }

}
