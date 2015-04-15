package net.bradmont.openmpd.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import net.bradmont.openmpd.R;

/**
 * Subclass of TextView to apply typefaces. Has XML attribute :fontName, 
 * which can be "default", "icons1", "icons2", or the name of a font file
 * in assets/fonts.
 */
public class FontTextView extends TextView {
    public static final String DEFAULT_FONT="Roboto-Light.ttf";
    public static final String ICONS1_FONT="Android-Dev-Icons-1.ttf";
    public static final String ICONS2_FONT="Android-Dev-Icons-2.ttf";
    public static final String MATERIAL_ICONS_FONT="material-icon-font.ttf";

    // since these are used repeatedly, cache them
    private static Typeface mDefaultTypeFace = null;
    private static Typeface mIcons1TypeFace = null;
    private static Typeface mIcons2TypeFace = null;
    private static Typeface mMaterialTypeFace = null;

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }
    
    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        
    }
    
    public FontTextView(Context context) {
        super(context);
        init(null);
    }
    
    private void init(AttributeSet attrs) {
        if (attrs!=null) {
             TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FontTextView);
             String fontName = a.getString(R.styleable.FontTextView_fontName);
             if (fontName == null || fontName.equals("default")){
                 fontName = DEFAULT_FONT;
             } else if (fontName.equals("icons1")){
                 fontName = ICONS1_FONT;
             } else if (fontName.equals("icons2")){
                 fontName = ICONS2_FONT;
             } else if (fontName.equals("material")){
                 fontName = MATERIAL_ICONS_FONT;
             }

             if (fontName.equals(DEFAULT_FONT)){
                 loadDefaults();
                 setTypeface(mDefaultTypeFace);
             } else if (fontName.equals(ICONS1_FONT)){
                 loadDefaults();
                 setTypeface(mIcons1TypeFace);
             } else if (fontName.equals(ICONS2_FONT)){
                 loadDefaults();
                 setTypeface(mIcons2TypeFace);
             } else if (fontName.equals(MATERIAL_ICONS_FONT)){
                 loadDefaults();
                 setTypeface(mMaterialTypeFace);
             } else {
                 Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+fontName);
                 setTypeface(myTypeface);
             }
             a.recycle();
        }
    }

    private void loadDefaults(){
        if (mDefaultTypeFace == null){
             mDefaultTypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+DEFAULT_FONT);
        }
        if (mIcons1TypeFace == null){
             mIcons1TypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+ICONS1_FONT);
        }
        if (mIcons2TypeFace == null){
             mIcons2TypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+ICONS2_FONT);
        }
        if (mMaterialTypeFace == null){
             mMaterialTypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+MATERIAL_ICONS_FONT);
        }
    }

}
