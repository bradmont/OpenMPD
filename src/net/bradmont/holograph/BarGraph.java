package net.bradmont.holograph;

import android.content.res.TypedArray;
import android.content.Context;

import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import android.util.AttributeSet;
import android.util.Log;

import android.view.View;
import net.bradmont.openmpd.R;

public class BarGraph extends View {

    protected int maxItems = 0;
    protected int minItems = 0;
    protected float barWeight = 1;
    protected float spacingWeight = 1;
    protected int [] colors = null;
    protected int lineColor = 0;

    protected Paint [] barPaints = null;
    protected Paint linePaint = null;

    protected float width;
    protected float height = 0f;
    protected float canvas_bottom = 0f;
    protected float canvas_left = 0f;
    protected float canvas_top = 0f;
    protected float canvas_right = 0f;
    protected float barWidth;
    protected float spacingWidth;
    protected float barHeightFactor;


    protected Object [][] values = new Integer [0][0];

    public BarGraph(Context context, AttributeSet attrs){
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs, R.styleable.BarGraph, 0,0);
        try {
            maxItems = a.getInteger(R.styleable.BarGraph_maxItems, 10);
            minItems = a.getInteger(R.styleable.BarGraph_minItems, 1);
            barWeight = a.getFloat(R.styleable.BarGraph_barWeight, 1);
            spacingWeight = a.getFloat(R.styleable.BarGraph_spacingWeight, 1);
            lineColor = a.getInteger(R.styleable.BarGraph_lineColor, 1);

            // colors is a comma separated list of color codes; process them.
            String colors_attr = a.getString(R.styleable.BarGraph_colors);
            if (colors_attr == null){ colors_attr =  "#33b5e5";}
            String [] colors_list = colors_attr.split(",");
            colors = new int [colors_list.length];
            for (int i = 0; i < colors_list.length; i++){
                colors[i] = Color.parseColor(colors_list[i]);
            }
        } finally {
            a.recycle();
        }
        init();
    }

    private void init(){
        barPaints = new Paint [colors.length];
        for (int i = 0; i < colors.length; i++){
            barPaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            barPaints[i].setColor(colors[i]);
            barPaints[i].setStyle(Paint.Style.FILL);
        }
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(lineColor);
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh){
        // Account for padding
        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        width = (float)w - xpad;
        height = (float)h - ypad;
        canvas_left = getPaddingLeft();
        canvas_bottom = height+getPaddingTop();
        canvas_right = getPaddingLeft()+width;
        canvas_top = getPaddingTop();

        // Figure out bar and spacing widths
        int bars = getBarCount();
        float factor = width / ((barWeight + spacingWeight)*bars);
        barWidth = factor * barWeight;
        spacingWidth = factor * spacingWeight;

        barHeightFactor = height / getMaxValue();
        setDrawingCacheEnabled(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int start = (values.length - maxItems) >0?values.length- maxItems:0;
        //Log.i("BarGraph", String.format("from %d to %d", start, values.length));

        // draw bars
        for (int i = start; i < values.length; i++){
            float bottom=canvas_bottom; // Bottom changes with each bar

            for (int j=0; j < values[i].length; j++){
                float val = 0f;
                if (values[i][j] instanceof Float){
                    val = ((Float) values[i][j]).floatValue();
                } else if (values[i][j] instanceof Integer){
                    val = ((Integer) values[i][j]).floatValue();
                }
                float left = (barWidth + spacingWidth) * (i-start) + (spacingWidth/2 + canvas_left);
                float right = left + barWidth;
                float top = bottom - val*barHeightFactor;
                //Log.i("BarGraph", String.format("value %f: %f %f %f %f", val, bottom, top, right, left));
                Rect r = new Rect((int)left, (int)top, (int)right, (int)bottom);
                canvas.drawRect(r, barPaints[j]);
                bottom = top; // stacked bars; bottom is top of previous
            }
        }

        // draw axes
        canvas.drawLine(canvas_left, canvas_bottom, canvas_left, canvas_top, linePaint);
        canvas.drawLine(canvas_left, canvas_bottom, canvas_right, canvas_bottom, linePaint);

        // cache it
        setDrawingCacheEnabled(true);
    }

    private float sumValues(Object [] list){
        float sum = 0f;
        for (int i = 0; i < list.length; i++){
            if (list[i] instanceof Float){
                sum += ((Float) list[i]).floatValue();
            } else if (list[i] instanceof Integer){
                sum += ((Integer) list[i]).floatValue();
            }
        }
        return sum;
    }

    /**
     * Returns the highest total value that needs to be displayed for
     * any visible element of the list
     */
    public float getMaxValue(){
        int start = (values.length - maxItems) >0?values.length- maxItems:0;
        if (values.length < 1){ return 0f;}

        float max = sumValues(values[start]);

        for (int i = start; i < values.length; i++){
            float val = sumValues(values[i]);
            if (val > max){
                max = val;
            }
        }
        //Log.i("BarGraph", String.format("Max: %f", max));
        return max;
    }

    public int getBarCount(){
       int numBars = values.length;
       if (numBars > maxItems) {
           return maxItems;
       } else  if (numBars < minItems) {
           return minItems;
       }
       return numBars;
    }

    // accessors

    public Object [][] getValues() {
        return values;
    }
    public void setValues(Object [] values) {
        if (values.length == 0){
            setValues(new Float[0][0]);
        } else {
            Object [][] vvs = new Object[values.length][1];
            for (int i = 0; i < values.length; i++){
                vvs[i][0] = values[i];
            }
            setValues(vvs);
        }
    }

    public void setValues(Object [][] values) {
        this.values = values;

        init();
        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();
        //Log.i("BarGraph", "setValues");
        barHeightFactor = height / getMaxValue();
    }
    public int getMaxItems() {
        return maxItems;
    }
    public void setMaxItems(int maxItems) {
        this.maxItems= maxItems;
        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();
    }
    public int getMinItems() {
        return minItems;
    }
    public void setMinItems(int minItems) {
        this.minItems= minItems;
        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();
    }
    public float getBarWeight() {
        return barWeight;
    }
    public void setBarWeight(int barWeight) {
        this.barWeight= barWeight;
        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();
    }
    public float getSpacingWeight() {
        return spacingWeight;
    }
    public void setSpacingWeight(int spacingWeight) {
        this.spacingWeight= spacingWeight;
        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();
    }
    public int [] getColors() {
        return colors;
    }
    public void setColors(int [] colors) {
        this.colors = new int [colors.length];
        for (int i = 0; i < colors.length; i++){
            this.colors[i] = colors[i];
        }
        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();
    }
}
