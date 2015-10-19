package com.svg.draw;

import java.io.File;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.constants.AppConstants;
import com.daimajia.androidanimations.library.Animate;
import com.daimajia.androidanimations.library.Techniques;
import com.mohom.drawsvg.R;
import com.svg.custom.SVGImageView;
import com.svg.custom.ZoomableRelativeLayout;

/**
 * {@link MainLauncherActivity} is a launcher activity which will be opened when
 * the user first clicks on the draw svg icon.
 * 
 * @author Mohammed Rampurawala
 * 
 * 
 */
@SuppressLint("ClickableViewAccessibility")
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class MainLauncherActivity extends Activity implements OnClickListener {

	private static final String TAG = MainLauncherActivity.class.getSimpleName();
	private static final int SD_CARD_REQUEST_CODE = 0;

	private SVGDrawingView mSvgView;
	private Dialog mDialog;
	private Button mImportSVGButton;
	private TextView mFileNameTextView;
	private SVGImageView mPreviewView;
	private ZoomableRelativeLayout mZoomableRelativeLayout;
	private Button mOkButton;
	private SVGImageView mFinalSVGImageView;
	private String mSVGUri;
	private int mImageResourceID;
	private Uri mImageURI;
	private ScaleGestureDetector mScaleGestureDetector;
	private Handler mWorkingHandler;
	private SVGPainter mSVGPainterThread;
	private Resources mAppResources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		mImportSVGButton.setOnClickListener(this);
		mSvgView.setSvgResource(mImageResourceID);
		runTheHandler();
	}

	/**
	 * @param toastMessage
	 *            Message to be displayed in toast.
	 */
	private void showToast(String toastMessage) {
		Toast.makeText(MainLauncherActivity.this, toastMessage, Toast.LENGTH_LONG).show();
	}

	/**
	 * Initialize all activity components.
	 */
	private void init() {
		mSvgView = (SVGDrawingView) findViewById(R.id.intro);
		mImportSVGButton = (Button) findViewById(R.id.import_svg);
		mFinalSVGImageView = (SVGImageView) findViewById(R.id.final_svg_image_view);
		mScaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), new OnPinchListener());
		mZoomableRelativeLayout = (ZoomableRelativeLayout) findViewById(R.id.zoomable_relative_layout);
		mZoomableRelativeLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mScaleGestureDetector.onTouchEvent(event);
				return true;
			}
		});
		mWorkingHandler = new Handler();
		mSVGPainterThread = new SVGPainter();
		mAppResources = getResources();
		mImageResourceID = R.raw.gear;

	}

	/**
	 * This will start the {@link SVGPainter} thread when the paths are drawn
	 * and display the svg image with fade in transition.
	 */
	public void runTheHandler() {
		if (mSVGPainterThread.isAlive()) {
			mSVGPainterThread.interrupt();
		}
		System.out.println("MainActivity.runTheHandler()");
		int time = AppConstants.DRAW_TIMER + 300;
		showToast("Draw time:" + String.valueOf(time));
		mFinalSVGImageView.setImageDrawable(getResources().getDrawable(R.drawable.transparent_drawable));
		mWorkingHandler.postDelayed(mSVGPainterThread, time);
	}

	/**
	 * Thread which will draw the SVG image with FadeIn transition.
	 * 
	 * @author mohammed.rampurawala
	 * 
	 */
	private class SVGPainter extends Thread {
		@Override
		public void run() {
			mFinalSVGImageView.setVisibility(View.VISIBLE);
			mFinalSVGImageView.setLayoutParams(new RelativeLayout.LayoutParams(SVGDrawingView.width,
					SVGDrawingView.height));
			if (mImageResourceID != 0) {
				mFinalSVGImageView.setImageResource(mImageResourceID);
			} else {
				mFinalSVGImageView.setImageURI(mImageURI);
			}
			Animate.with(Techniques.FadeIn).duration(AppConstants.TRANSITION_TIME).playOn(mFinalSVGImageView);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SD_CARD_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
			String tempUri = data.getData().toString();
			String filename = tempUri.substring((tempUri.lastIndexOf(File.separator) + 1), tempUri.length());
			Log.w(TAG, "filename:" + filename);
			if (!filename.endsWith(".svg")) {
				/*
				 * if image doesn't endswith .svg then image will not be set in
				 * in the SVGImageView of the Dialog and the error will be shown
				 * in fileNameTextView that its not a valid file.
				 */
				mFileNameTextView.setError(getResources().getString(R.string.svg_error));
				mFileNameTextView.setText(getResources().getString(R.string.svg_error));
				mOkButton.setEnabled(false);
			} else {
				/*
				 * if the image is really an svg image then it will parse the
				 * imageURi and pass it to the androidSVG and will create the
				 * path. and set the imageResourceID to 0 and sampleSVGView is
				 * the SVGImageView on the Dialog and fileNameTextView to
				 * filename and its error to null. and uri is used for passing
				 * the full filepath to svgHelper class and there the image will
				 * be drawn. and the main thing OKButton is set to enabled
				 * because if the SVG image is valid then only the image will be
				 * set otherwise the image will not be set. and the
				 * finalSVGImageView which will show the final color of the
				 * image that will be set to GONE i.e; its visibility to GONE.
				 */
				mImageURI = data.getData();
				mImageResourceID = 0;
				mPreviewView.setImageURI(data.getData());
				mFileNameTextView.setError(null);
				mFileNameTextView.setText(filename);
				mSVGUri = tempUri;
				mOkButton.setEnabled(true);
				mFinalSVGImageView.setVisibility(View.GONE);

			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * Listener for zoom in and zoom out.
	 * 
	 * @author mohammed.rampurawala
	 * 
	 */
	public class OnPinchListener extends SimpleOnScaleGestureListener {
		private float startingSpan;
		private float endSpan;
		private float startFocusX;
		private float startFocusY;

		public boolean onScaleBegin(ScaleGestureDetector detector) {
			startingSpan = detector.getCurrentSpan();
			startFocusX = detector.getFocusX();
			startFocusY = detector.getFocusY();
			return true;
		}

		public boolean onScale(ScaleGestureDetector detector) {
			mZoomableRelativeLayout.scale(detector.getCurrentSpan() / startingSpan, startFocusX, startFocusY);

			// mZoomableRelativeLayout.scale(scaleFactor, startFocusX,
			// startFocusX);

			return true;
		}

		public void onScaleEnd(ScaleGestureDetector detector) {
			// mZoomableRelativeLayout.restore();
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.ok_select:
			if (mSVGUri != null) {
				if (mDialog != null) {
					mDialog.dismiss();
					mFinalSVGImageView.setVisibility(View.GONE);
					mSvgView.setVisibility(View.VISIBLE);
					mSvgView.setSvgResource(mSVGUri);
					runTheHandler();
				}
			}
			break;
		case R.id.import_svg:
			mDialog = new Dialog(MainLauncherActivity.this);
			mDialog.setContentView(R.layout.select_svg);

			Button selectImage = (Button) mDialog.findViewById(R.id.file_select);
			Button cancelButton = (Button) mDialog.findViewById(R.id.cancel_select);
			cancelButton.setOnClickListener(MainLauncherActivity.this);
			mPreviewView = (SVGImageView) mDialog.findViewById(R.id.sample_image_svg);
			mOkButton = (Button) mDialog.findViewById(R.id.ok_select);
			mFileNameTextView = (TextView) mDialog.findViewById(R.id.file_name_text_view);
			mOkButton.setOnClickListener(this);

			selectImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mSvgView.setVisibility(View.GONE);
					showToast(mAppResources.getString(R.string.select_hint));
					try {
						Intent intent = new Intent(android.content.Intent.ACTION_GET_CONTENT);
						intent.setType("file/*");
						startActivityForResult(intent, SD_CARD_REQUEST_CODE);
					} catch (Exception e) {
						e.printStackTrace();
						showToast(mAppResources.getString(R.string.select_error));
					}
				}
			});
			mDialog.setTitle(mAppResources.getString(R.string.select_svg));
			mDialog.setCancelable(true);
			mDialog.setCanceledOnTouchOutside(true);
			mDialog.show();
			break;

		case R.id.cancel_select:
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			break;
		}

	}

}
