package com.example.combiflash;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class AppFunctions {

    /**
     * Sets drawables to provided textview according to their colors
     *
     * @param textView       The concerned textview
     * @param drawableLeft   The resource id for drawable to be set on left
     * @param colorLeft      The resource id for the color to be set to left drawable
     * @param drawableTop    The resource id for drawable to be set on top
     * @param colorTop       The resource id for the color to be set to top drawable
     * @param drawableRight  The resource id for drawable to be set on right
     * @param colorRight     The resource id for the color to be set to right drawable
     * @param drawableBottom The resource id for drawable to be set on bottom
     * @param colorBottom    The resource id for the color to be set to bottom drawable
     * @author Harsh Dhar Agarwal
     */
    static void setTextDrawables(@NonNull TextView textView, int drawableLeft, int colorLeft, int drawableTop, int colorTop, int drawableRight, int colorRight, int drawableBottom, int colorBottom) {
        textView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
        Drawable[] drawables = textView.getCompoundDrawables();
        if (drawables[0] != null)
            drawables[0].setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), colorLeft), PorterDuff.Mode.SRC_IN));
        if (drawables[1] != null)
            drawables[1].setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), colorTop), PorterDuff.Mode.SRC_IN));
        if (drawables[2] != null)
            drawables[2].setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), colorRight), PorterDuff.Mode.SRC_IN));
        if (drawables[3] != null)
            drawables[3].setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), colorBottom), PorterDuff.Mode.SRC_IN));
    }

    /**
     * Get a list of distinct colors
     *
     * @param num The number of colors required
     * @return ArrayList of colors
     * @author Harsh Dhar Agarwal
     */
    static ArrayList<Integer> getDistinctColors(int num) {
        int baseColor = Color.parseColor("#53FFA7");
        ArrayList<Integer> colors = new ArrayList<Integer>(num);
        float[] hsv = new float[3];
        Color.RGBToHSV(
                Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor),
                hsv
        );
        double step = 240.0 / num;
        float baseHue = hsv[0];
        for (int i=1;i<=num;i++) {
            float nextColorHue = (float)(baseHue + step * (float)i) % 240.0f;
            colors.add(Color.HSVToColor(new float[]{nextColorHue, hsv[1], hsv[2]}));
        }
        return colors;
    }
}
