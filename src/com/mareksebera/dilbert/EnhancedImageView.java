package com.mareksebera.dilbert;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Taken from:
 * "http://stackoverflow.com/questions/4677269/how-to-stretch-three-images-across-the-screen-preserving-aspect-ratio"
 * Credits to: "http://stackoverflow.com/users/496992/patrick-boos"
 * */
public class EnhancedImageView extends ImageView {

	public EnhancedImageView(Context context) {
		super(context);
	}

	public EnhancedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EnhancedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (getDrawable() != null) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
			setMeasuredDimension(width, height);
		} else {
			setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		}
	}
}