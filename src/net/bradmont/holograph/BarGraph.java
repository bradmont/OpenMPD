package net.bradmont.holograph;

import android.content.res.TypedArray;
import android.content.Context;

import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import android.util.AttributeSet;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;

import java.lang.Math;

import net.bradmont.openmpd.R;

public class BarGraph extends View {

    /* Defaults that can be overridden in XML */
    protected int maxItems = 0; // max items to show at a time
    protected int minItems = 0; // min items to show at a time
    protected float barWeight = 1; // relative weight of bars
    protected float spacingWeight = 1; // relative weight of space between bars
    protected int [] colors = null; 
    protected int lineColor = 0;
    protected int maxTicks = 8; // maximum number of value labels


    protected Paint [] barPaints = null;
    protected Paint linePaint = null;

    protected float width;
    protected float height = 0f;
    protected float canvas_bottom = 0f;
    protected float data_bottom = 0f;

    protected float canvas_left = 0f;
    protected float data_left = 0f;

    protected float canvas_top = 0f;
    protected float canvas_right = 0f;
    protected float barWidth;
    protected float spacingWidth;
    protected float barHeightFactor;

    protected float maxValue = 0;
    protected float tickSpacing = 0;
    protected float tickWidth = 0;

    // swipe related:
    protected float mTranslationX = 0;
    protected float mDownX = 0;


    protected Object [][] values = new Integer [0][0];
    protected String [] labels = null;

    public BarGraph(Context context, AttributeSet attrs){
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs, R.styleable.BarGraph, 0,0);
        try {
            // get values from XML
            maxItems = a.getInteger(R.styleable.BarGraph_maxItems, 10);
            minItems = a.getInteger(R.styleable.BarGraph_minItems, 1);
            barWeight = a.getFloat(R.styleable.BarGraph_barWeight, 1);
            spacingWeight = a.getFloat(R.styleable.BarGraph_spacingWeight, 1);
            lineColor = a.getInteger(R.styleable.BarGraph_lineColor, 1);
            maxTicks = a.getInteger(R.styleable.BarGraph_maxTicks, 8);

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

        String sampleLabel = Integer.toString( maxTicks * (int)tickSpacing);
        float label_width = linePaint.measureText( sampleLabel);



        // Figure out bar and spacing widths
        int bars = getBarCount();
        float barWidths = bars + (.33f * 1.5f);
        //float factor = width / ((barWeight*barWidths + spacingWeight*bars));
        float factor = (width-label_width) / ((barWeight*barWidths + spacingWeight*bars));
        barWidth = factor * barWeight;
        tickWidth = barWidth * .33f;
        spacingWidth = factor * spacingWeight;

        // boundaries for data
        data_left = canvas_left + label_width + (tickWidth * 1.5f);

        if (labels != null){
            Rect r = new Rect();
            linePaint.getTextBounds(sampleLabel, 0, sampleLabel.length() - 1, r);
            float label_height = Math.abs(r.height());
            // Labels at angle, calculate height:
            label_height = (label_width + label_height) /(float) Math.sqrt(2);
            data_bottom = canvas_bottom - (label_height + tickWidth);
            barHeightFactor = (height-label_height - tickWidth) / maxValue;
        } else {
            data_bottom = canvas_bottom ;
            barHeightFactor = height / maxValue;
        }

        setDrawingCacheEnabled(false);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw bars
        for (int i = 0; i < values.length ; i++){

            int index = values.length - i -1;
            float bottom=data_bottom; // Bottom changes with each bar
            float right = canvas_right - spacingWidth/2 - i * (barWidth + spacingWidth) - mTranslationX;
            float left = right - barWidth;
            float label_left = left;

            if (left < data_left){
                left = data_left;
            }
            if (right > canvas_right){
                right = canvas_right;
            }
            // don't draw if bar is outside data area
            if (left < canvas_right && right > data_left){
                // draw the bar in segments
                for (int j=0; j < values[index].length; j++){
                    float val = 0f;
                    if (values[index][j] instanceof Float){
                        val = ((Float) values[index][j]).floatValue();
                    } else if (values[index][j] instanceof Integer){
                        val = ((Integer) values[index][j]).floatValue();
                    }
                    float top = bottom - val*barHeightFactor;
                    //Log.i("net.bradmont.holograph", String.format("value %f: %f %f %f %f", val, bottom, top, right, left));
                    Rect r = new Rect((int)left, (int)top, (int)right, (int)bottom);
                    canvas.drawRect(r, barPaints[j % barPaints.length]);
                    bottom = top; // stacked bars; bottom is top of previous
                }
                if (labels != null){
                    String label = labels[index];
                    canvas.save();
                    canvas.rotate(-45, label_left, canvas_bottom);
                    canvas.drawText(label , label_left, canvas_bottom, linePaint);
                    canvas.restore();
                }
            }
        }

        // draw axes
        canvas.drawLine(data_left, data_bottom, data_left, canvas_top, linePaint);
        canvas.drawLine(data_left, data_bottom, canvas_right, data_bottom, linePaint);
        //Log.i("net.bradmont.holograph", String.format("ticks: %d; tickSpacing %f", maxTicks, tickSpacing));
        Rect r = new Rect();
        for (int tick = 0; tick <= maxTicks; tick++){
            float y = data_bottom - (tick * tickSpacing*barHeightFactor);

            if (maxTicks % 2 == 0 && tick % 2 == 0){
                canvas.drawLine(data_left, y, data_left - tickWidth, y, linePaint);
                String label = Integer.toString (tick* (int)tickSpacing);
                linePaint.getTextBounds(label, 0, label.length() - 1, r);
                if (tick == 0){
                    canvas.drawText(label , canvas_left, y, linePaint);
                } else {
                    y = y - r.centerY();
                    canvas.drawText(label , canvas_left, y, linePaint);
                }
            } else {
                canvas.drawLine(data_left, y, data_left - (tickWidth/2), y, linePaint);
            }
        }

        // cache it
        setDrawingCacheEnabled(true);
    }
    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = mTranslationX + motionEvent.getX();
                return true;
            //case MotionEvent.ACTION_CANCEL: 
            case MotionEvent.ACTION_UP: 
                mTranslationX = (mDownX - motionEvent.getX()) ;
                invalidate();
                if (mTranslationX > 0){ mTranslationX = 0;} // if we scrolled past right edge, snap back
                if (mTranslationX < -1 * (values.length - getBarCount() )*(barWidth+spacingWidth) ){ 
                    // if we scrolled past left edge, snap back
                    mTranslationX = -1 * (values.length - getBarCount() )*(barWidth+spacingWidth);
                }
                invalidate();
                return true;
           
            case MotionEvent.ACTION_MOVE: 
                mTranslationX = (mDownX - motionEvent.getX()) ;
                if (mTranslationX > 0){ mTranslationX = 0;} // don't scroll past right edge
                if (mTranslationX < -1 * (values.length - getBarCount() )*(barWidth+spacingWidth) ){ 
                    // don't scroll past left edge
                    mTranslationX = -1 * (values.length - getBarCount() )*(barWidth+spacingWidth);
                }

                invalidate();
                return true;
        }
        return false;
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

        maxValue = getMaxValue();
        calcNiceTicks(maxValue);
        barHeightFactor = height / maxValue;

        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();
    }

    public void setLabels(String [] labels){
        this.labels = labels;
        init();

        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();

    }

    /* Pretty ticks algorithm thanks to Steffen L Norgen, at
    http://www.esurient-systems.ca/2011/03/algorithm-for-optimal-scaling-on-chart_8199.html
     */
    protected void calcNiceTicks(float max){
        tickSpacing = prettyValue (maxValue / maxTicks -1, false);
        maxValue = (float) Math.ceil( maxValue / tickSpacing) * tickSpacing;
    }

    protected float prettyValue(float range, boolean round){
        double exponent;
        double fraction;
        double niceFraction;

        exponent = Math.floor(Math.log10(range));
        fraction = range/Math.pow(10, exponent);

        if (round){
            if (fraction < 1.5){
                niceFraction = 1;
            } else if (fraction < 3){
                niceFraction = 2;
            } else if (fraction < 7){
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        } else {
            if (fraction <= 1){
                niceFraction = 1;
            } else if (fraction <= 2.01){
                niceFraction = 2;
            } else if (fraction <= 5.01){
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        }
        return (float) (niceFraction * Math.pow(10, exponent));
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
