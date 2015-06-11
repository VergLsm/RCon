package org.verg.rcon;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by verg on 15/5/24.
 */
public class VerticalProgressBar extends ProgressBar {

    public VerticalProgressBar(Context context) {
        super(context);
        // TODO 自动生成的构造函数存根
    }

    public VerticalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO 自动生成的构造函数存根
    }

    public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO 自动生成的构造函数存根
    }

    /*
     * （非 Javadoc）
     * 
     * @see android.widget.ProgressBar#onDraw(android.graphics.Canvas)
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.rotate(-90);// 反转90度，将水平ProgressBar竖起来
        canvas.translate(-getHeight(), 0);// 将经过旋转后得到的VerticalProgressBar移到正确的位置,注意经旋转
        // 后宽高值互换
        super.onDraw(canvas);
    }

    /*
     * （非 Javadoc）
     * 
     * @see android.widget.ProgressBar#onMeasure(int, int)
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());//互换宽高值
    }

    /*
     * （非 Javadoc）
     * 
     * @see android.widget.ProgressBar#onSizeChanged(int, int, int, int)
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO 自动生成的方法存根
        super.onSizeChanged(h, w, oldw, oldh);//互换宽高值
    }

}