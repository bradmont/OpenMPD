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
    protected int ticks = maxTicks; // maximum number of value labels
    protected int labelTextSize = 16;


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
    protected int labelZeros = -1;

    // swipe related:
    protected float mTranslationX = 0;
    protected float mDownX = 0;

    private static final String [] suffixes = {"", "0", "00", "k", "0k", "00k", "M", "0M", "00M", "G" };


    protected Object [][] values = new Integer [0][0];
    protected String [] labels = null;
    protected String [] groups = null;
    protected int [] groupLabelOffset = null;

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
            ticks = maxTicks;
            labelTextSize = a.getDimensionPixelSize(R.styleable.BarGraph_labelTextSize, 16);

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
        linePaint.setTextSize(labelTextSize);
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

        String sampleLabel = makeBiggestLabel(ticks, (int)tickSpacing);
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
        String extraLabelRight="";
        String extraLabelLeft = "";
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
                if (groups != null){
                    // draw group label line if it goes here
                    String group = groups[index];
                    Rect r = new Rect();
                    linePaint.getTextBounds(group, 0, group.length() - 1, r);
                    float y = data_bottom - ((ticks-1) * tickSpacing*barHeightFactor) + r.height();

                    if (groupLabelOffset[index] == 0){
                        float x = right -barWidth ;
                        if (x < data_left + spacingWidth){
                            x = data_left + spacingWidth;
                        }
                        canvas.drawText(group , x, y, linePaint);
                    } else if (!extraLabelLeft.equals(group)&&
                        // left-and label when necessary
                            left - (barWidth + spacingWidth) * (groupLabelOffset[index])
                                < data_left){
                        float x = data_left + spacingWidth;
                        canvas.drawText(group , x, y, linePaint);
                        extraLabelLeft = group;
                    } else if (!extraLabelRight.equals(group) &&
                        // right-hand label when necessary
                        right - (barWidth + spacingWidth) * (groupLabelOffset[index] +1)
                            > canvas_right - spacingWidth - r.width()){
                        float x = canvas_right - barWidth;
                        canvas.drawText(group , x, y, linePaint);
                        extraLabelRight = group;
                    }
                    
                    // draw group divider line if it goes here
                    if (index!=0 && !groups[index-1].equals(groups[index])){
                        if (left - spacingWidth/2 > data_left ){
                            canvas.drawLine(left - (spacingWidth/2), data_bottom, 
                                            left - (spacingWidth/2), canvas_top, linePaint);
                        }
                    }
                    /*else if (index!=0 && !groups[index-1].equals(groups[index])){
                        float y = data_bottom - (maxTicks * tickSpacing*barHeightFactor);
                        String group = groups[index];
                        Rect r = new Rect();
                        linePaint.getTextBounds(group, 0, group.length() - 1, r);
                        y = y - r.centerY();
                        float x = left;
                        if (right-barWidth < data_left){
                            x = right-barWidth;
                        }
                        canvas.drawText(group , left, y, linePaint);
                    }*/
                }
            }
        }

        // draw axes
        canvas.drawLine(data_left, data_bottom, data_left, canvas_top, linePaint);
        canvas.drawLine(data_left, data_bottom, canvas_right, data_bottom, linePaint);
        //Log.i("net.bradmont.holograph", String.format("ticks: %d; tickSpacing %f", maxTicks, tickSpacing));
        Rect r = new Rect();
        for (int tick = 0; tick < ticks; tick++){
            float y = data_bottom - (tick * tickSpacing*barHeightFactor);

            if (ticks % 2 != tick % 2 ){
                // label every other tick; start at 0 if we have an odd
                // number of ticks, else start at first non-zero tick
                // this way the top tick always has a label
                canvas.drawLine(data_left, y, data_left - tickWidth, y, linePaint);
                String label = makeLabel (tick* (int)tickSpacing);
                linePaint.getTextBounds(label, 0, label.length() - 1, r);
                if (tick == ticks - 1) {
                    y = y + r.height();
                } else if (tick != 0) {
                    y = y - r.centerY();
                }
                canvas.drawText(label , canvas_left, y, linePaint);
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
                getParent().requestDisallowInterceptTouchEvent(true);
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
    public void setGroups(String [] groups){
        this.groups = groups;
        groupLabelOffset = new int[groups.length];
        for (int i = 0; i < groups.length; i++){

            int group_before = 0;
            int group_after = 0;
            // how many group members before & after this element
            while (i+group_after < groups.length 
                    && groups[i+group_after].equals(groups[i])){
                group_after++;
            }
            while (i-group_before >= 0
                    && groups[i-group_before].equals(groups[i])){
                group_before++;
            }
            // how far is this element from the label?
            if ((group_after + group_before +1) % 2 == 0){
                    groupLabelOffset[i] = (group_before - group_after+1) / 2;
            } else {
                    groupLabelOffset[i] = (group_before - group_after) / 2;
            }
        }
        init();

        setDrawingCacheEnabled(false);
        invalidate();
        requestLayout();
    }

    /* Pretty ticks algorithm thanks to Steffen L Norgen, at
    http://www.esurient-systems.ca/2011/03/algorithm-for-optimal-scaling-on-chart_8199.html
     */
    protected void calcNiceTicks(float max){
        tickSpacing = prettyValue (maxValue / (maxTicks -1), false);
        ticks = (int) Math.ceil(maxValue / tickSpacing) + 1;
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

    private String makeLabel(int label){
        String result = Integer.toString(label);
        if (result.length() <= labelZeros){
            return result;
        }
        try {
            result = result.substring(0, result.length() - labelZeros);
        } catch (java.lang.StringIndexOutOfBoundsException e){
            return result;
        }
        return result + suffixes[labelZeros];
    }

    private String makeBiggestLabel(int ticks, int tickSpacing){
        labelZeros = countZeros(Integer.toString( tickSpacing * (ticks - 1)));

        int biggest_label = ticks;
        float label_width = linePaint.measureText(Integer.toString( tickSpacing * ticks));

        for (int i = 1; i < ticks; i++){
            if (ticks % 2 != i % 2 ){
                if (countZeros(Integer.toString( tickSpacing * i)) < labelZeros){
                    labelZeros = countZeros(Integer.toString( tickSpacing * i));
                }
                if (linePaint.measureText(Integer.toString( tickSpacing * i)) > label_width){
                    biggest_label = i;
                    label_width = linePaint.measureText(Integer.toString( tickSpacing * i));
                }
            }
        }
        if (labelZeros >= suffixes.length){
            labelZeros = suffixes.length - 1;
        }
        return makeLabel(biggest_label * tickSpacing);
    }

    /* counts the 0s at the end of a string
     */
    private int countZeros(String number){
        int result = 0;
        while (number.endsWith("0")){
            result++;
            number = number.substring(0, number.length()-1);
        }
        return result;
    }

}
