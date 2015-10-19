package com.svg.draw;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Stack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

import com.svg.base.PreserveAspectRatio;
import com.svg.base.SVG;
import com.svg.base.SVGParseException;

/**
 * {@link SVGHelper} class is used to parse the SVG image from the
 * {@link InputStream}.
 * 
 * @author Mohammed Rampurawala
 * 
 * 
 */
public class SVGHelper {
	private static final String TAG = SVGHelper.class.getSimpleName();

	private final Stack<SvgPath> mPaths = new Stack<SvgPath>();
	private final Paint mSourcePaint;

	private SVG mSvg;

	static int count;

	Path myPath = new Path();

	public SVGHelper(Paint sourcePaint) {
		mSourcePaint = sourcePaint;
	}

	public void load(Context context, int svgResource) {
		if (mSvg != null)
			return;
		try {
			mSvg = SVG.getFromResource(context, svgResource);

			mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.UNSCALED);
		} catch (SVGParseException e) {
			Log.e(TAG, "Could not load specified SVG resource", e);
		}
	}

	public void load(Context context, String filePath) {

		mSvg = null;

		// Log.w("Valid", "Input Stream");
		try {

			if (mPaths.size() != 0) {

				Log.w(TAG, "mpaths empty");
				mPaths.clear();

			}

			filePath = filePath.replace("file://", "");
			Log.w(TAG, "FilePath:" + filePath);

			File file = new File(filePath);

			FileInputStream fis = new FileInputStream(file);

			//

			mSvg = SVG.getFromInputStream(fis);

			mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.UNSCALED);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class SvgPath {
		private static final Region sRegion = new Region();
		private static final Region sMaxClip = new Region(Integer.MIN_VALUE,
				Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

		public final Path path;
		public final Paint paint;
		public final float length;
		public final Rect bounds;

		SvgPath(Path path, Paint paint) {
			this.path = path;
			this.paint = paint;

			PathMeasure measure = new PathMeasure(path, false);
			this.length = measure.getLength();

			sRegion.setPath(path, sMaxClip);
			bounds = sRegion.getBounds();
		}
	}

	public Stack<SvgPath> getPathsForViewport(final int width, final int height) {
		mPaths.clear();

		Canvas canvas = new Canvas() {
			private final Matrix mMatrix = new Matrix();

			@Override
			public int getWidth() {
				return width;
			}

			@Override
			public int getHeight() {
				return height;
			}

			@SuppressWarnings({ "deprecation", "deprecation" })
			@Override
			public void drawPath(Path path, Paint paint) {
				Path dst = new Path();
				// no inspection deprecation
				getMatrix(mMatrix);
				path.transform(mMatrix, dst);

				mPaths.add(new SvgPath(dst, new Paint(mSourcePaint)));
			}
		};
		try {
			RectF viewBox = mSvg.getDocumentViewBox();
			float scale = Math.min(width / viewBox.width(),
					height / viewBox.height());

			canvas.translate((width - viewBox.width() * scale) / 2.0f,
					(height - viewBox.height() * scale) / 2.0f);
			canvas.scale(scale, scale);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mSvg.renderToCanvas(canvas);

		return mPaths;
	}

}
