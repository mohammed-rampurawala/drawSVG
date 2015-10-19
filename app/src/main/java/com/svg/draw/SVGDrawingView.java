package com.svg.draw;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.constants.AppConstants;
import com.mohom.drawsvg.R;
import com.svg.adapter.AnimationAdapter;

/**
 * @author Mohammed Rampurawala
 * 
 * 
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class SVGDrawingView extends View implements Runnable {

	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final SVGHelper mSvg = new SVGHelper(mPaint);
	private static final String TAG = SVGDrawingView.class.getSimpleName();
	private static int counter = 0;
	public static int width;
	public static int height;
	private int mSvgResource;
	private String mSvgResourcePath;
	private int widthOfViewPort;
	private int heightOfViewPort;
	private int timer = 20000;
	private Stack<SVGHelper.SvgPath> mPaths = new Stack<SVGHelper.SvgPath>();
	private List<ObjectAnimator> mObjectAnimator = new ArrayList<ObjectAnimator>();
	private Thread mLoader;
	private Thread svgInputLoader;
	private float mPhase;
	private TypedArray a;
	private Object mSvgLock = new Object();
	private static int sizeOfPaths;
	private static int j = 0;

	public SVGDrawingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SVGDrawingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		a = context.obtainStyledAttributes(attrs, R.styleable.SVGDrawingView, defStyle, 0);
		try {
			if (a != null) {
				mPaint.setStrokeWidth(4.0f);
				mPaint.setColor(0xff000000);
				mPhase = a.getFloat(R.styleable.SVGDrawingView_phase, 0.0f);
			}
		} finally {
			if (a != null)
				a.recycle();
		}

		init();
	}

	private void init() {
		mPaint.setStyle(Paint.Style.STROKE);
		setLayerType(LAYER_TYPE_SOFTWARE, null);

	}

	public void setSvgResource(int resource) {
		if (mSvgResource == 0) {
			mSvgResource = resource;
		}
	}

	public void setSvgResource(String filePath) {
		try {
			// if (mSvgResourcePath.equals(null))
			// {
			mSvgResourcePath = filePath;
			svgInputLoader = new Thread(this);
			Log.w(TAG, "Setting SVG:" + filePath);

			svgInputLoader.start();
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		// synchronized (mSvgLock) {
		canvas.save();

		for (; j < mPaths.size();) {
			// Log.w("In Loop", "InLOop");

			for (int pathsDrawnOnCanvas = 0; pathsDrawnOnCanvas < j; pathsDrawnOnCanvas++) {

				canvas.drawPath(mPaths.get(pathsDrawnOnCanvas).path, mPaths.get(pathsDrawnOnCanvas).paint);

			}

			SVGHelper.SvgPath svgPath1 = mPaths.get(j);

			svgPath1.paint.setAlpha(255);
			canvas.drawPath(svgPath1.path, svgPath1.paint);
			break;

		}

		canvas.restore();
		// }
	}

	@Override
	protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {

		super.onSizeChanged(w, h, oldw, oldh);
		// Log.w("width", String.valueOf(w));
		// Log.w("Height", String.valueOf(h));
		if (mLoader != null) {
			try {
				mLoader.join();

			} catch (InterruptedException e) {
				Log.e(TAG, "Unexpected error", e);
			}
		}

		mLoader = new Thread(new Runnable() {

			@Override
			public void run() {
				width = w;
				height = h;

				widthOfViewPort = w - getPaddingLeft() - getPaddingRight();

				heightOfViewPort = h - getPaddingTop() - getPaddingBottom();

				mSvg.load(getContext(), mSvgResource);

				synchronized (mSvgLock) {
					mPaths = mSvg.getPathsForViewport(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop()
							- getPaddingBottom());

					// sizeOfPaths = mPaths.size();
					// Toast.makeText(context, String.valueOf(sizeOfPaths),
					// 1000).show();
					// System.out.println("MySvgView.onSizeChanged()"+String.valueOf(sizeOfPaths));
					addAnimationListernerToObjectAnimator();

				}
				post(new Runnable() {
					@Override
					public void run() {
						j = 0;
						mObjectAnimator.get(j).start();

					}
				});
			}
		}, "Loading SVG");
		mLoader.start();
	}

	private void updatePathsPhaseLocked() {
		for (; j < mPaths.size();) {
			SVGHelper.SvgPath svgPath = mPaths.get(j);
			svgPath.paint.setPathEffect(createPathEffect(svgPath.length, mPhase, 0.0f));
			break;
		}
	}

	//
	public void setPhase(float phase) {

		mPhase = phase;

		synchronized (mSvgLock) {
			updatePathsPhaseLocked();
		}
		invalidate();
	}

	// Applying path effect to line while drawing the SVG Image

	private static PathEffect createPathEffect(float pathLength, float phase, float offset) {

		return new DashPathEffect(new float[] { pathLength, pathLength }, Math.max(phase * pathLength, offset));
	}

	@Override
	public void run() {
		svgInputLoader = new Thread(new Runnable() {

			@Override
			public void run() {

				mSvg.load(getContext(), mSvgResourcePath);

				if (mPaths.size() != 0) {
					mPaths.clear();
				}

				mPaths = mSvg.getPathsForViewport(widthOfViewPort, heightOfViewPort);

				// sizeOfPaths = mPaths.size();

				addAnimationListernerToObjectAnimator();

				post(new Runnable() {
					@Override
					public void run() {
						j = 0;
						mObjectAnimator.get(j).start();

					}
				});
			}
		}, "Loading SVG From InputStream");
		svgInputLoader.start();
	}

	public void addAnimationListernerToObjectAnimator() {

		if (mObjectAnimator.size() != 0) {
			mObjectAnimator.clear();
		}

		Log.w(TAG, String.valueOf(mPaths.size()) + ": Paths");

		if (AppConstants.DRAW_TIMER > 5000) {

			timer = AppConstants.DRAW_TIMER / mPaths.size();
		}

		Log.w(TAG, String.valueOf(timer) + "Timer Value");
		for (int i = 0; i < mPaths.size(); i++) {
			// Log.w("Element Added", "Element Added");
			mObjectAnimator.add(ObjectAnimator.ofFloat(SVGDrawingView.this, "phase", 1.0f, 0.0f).setDuration(timer));
		}
		// MyConstants.DRAW_TIMER = 2000*mPaths.size();

		/*
		 * Running the Handler
		 */
		// context.runTheHandler();

		for (int i = 0; i < mObjectAnimator.size(); i++) {

			mObjectAnimator.get(i).addListener(new AnimationAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					/*
					 * I have given -1 because the loop will go one step extra
					 * and will cause the error in choreographer class and
					 */

					// Toast.makeText(context, ""+mPaths.size(),500).show();
					if (j < mPaths.size() - 1) {
						++j;
						// System.out
						// .println("MySvgView.addAnimationListernerToObjectAnimator().new AnimationAdapter()"+j);
						mObjectAnimator.get(j).start();

					}
				}
			});
		}

	}

}