package com.svg.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 
 * Dummy {@link RelativeLayout} for gestures.
 * 
 * @author Mohammed Rampurawala
 * 
 * 
 */
public class ZoomableRelativeLayout extends RelativeLayout {

	private float mScaleFactor = 1;
	private float mPivotX;
	private float mPivotY;

	public ZoomableRelativeLayout(Context context) {
		super(context);
	}

	public ZoomableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ZoomableRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@SuppressWarnings("deprecation")
	protected void dispatchDraw(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		// Matrix matrix = canvas.getMatrix();
		// canvas.setMatrix(matrix);
		// Log.e("Touched", "touching");
		canvas.scale(mScaleFactor, mScaleFactor, mPivotX, mPivotY);

		super.dispatchDraw(canvas);
		canvas.restore();
	}

	public void scale(float scaleFactor, float pivotX, float pivotY) {
		mScaleFactor = scaleFactor;
		mPivotX = pivotX;
		mPivotY = pivotY;
		this.invalidate();
	}

	public void restore() {
		mScaleFactor = 1;
		this.invalidate();
	}

}
