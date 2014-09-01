/*
 * Copyright © 2014 Zhisheng Zhou.
 */
package edu.nyu.zhisheng.sensorsexplorer.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GpsSatellite;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Displays the signal-to-noise ratio of the GPS satellites in a bar chart.
 */
public class GpsSnrView extends View {
	private String TAG = "GpsSnrView";

	private Iterable<GpsSatellite> mSats;

	private Paint activePaint;
	private Paint inactivePaint;
	private Paint gridPaint;
	private Paint gridPaintStrong;

	// FIXME: should be DPI-dependent, this is OK for MDPI
	private int gridStrokeWidth = 2;

	// Which satellites to draw
	private boolean draw32 = true; // 01-32 (GPS satellites)
	private boolean draw64 = false; // 33-54 are not used
	private boolean draw88 = false; // 65-88 (GLONASS satellites)

	/**
	 * @param context
	 */
	public GpsSnrView(Context context) {
		super(context);
		doInit();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public GpsSnrView(Context context, AttributeSet attrs) {
		super(context, attrs);
		doInit();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public GpsSnrView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		doInit();
	}

	private void doInit() {
		activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		activePaint.setColor(Color.parseColor("#FF33B5E5"));
		activePaint.setStyle(Paint.Style.FILL);

		inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		inactivePaint.setColor(Color.parseColor("#FFFF4444"));
		inactivePaint.setStyle(Paint.Style.FILL);

		gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		gridPaint.setColor(Color.parseColor("#FF4D4D4D"));
		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setStrokeWidth(gridStrokeWidth);

		gridPaintStrong = new Paint(gridPaint);
		gridPaintStrong.setColor(Color.parseColor("#FFFFFFFF"));
	}

	/**
	 * Returns the number of SNR bars to draw
	 * 
	 * The number of bars to draw varies depending on the systems supported by
	 * the device. The most common numbers are 32 for a GPS-only receiver or 56
	 * for a combined GPS/GLONASS receiver.
	 * 
	 * @return The number of bars to draw
	 */
	private Integer getNumBars() {
		return (draw32 ? 32 : 0) + (draw64 ? 32 : 0) + (draw88 ? 24 : 0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int mHeight = (int) Math.min(
				MeasureSpec.getSize(widthMeasureSpec) * 0.15f,
				MeasureSpec.getSize(heightMeasureSpec));
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
	}

	/*
	 * Draws a satellite's SNR bar.
	 */
	private void drawSat(Canvas canvas, int prn, float snr, boolean used) {
		int w = getWidth();
		int h = getHeight();

		int i = draw32 ? prn : (prn - 32);
		if ((i >= 64) && !draw64)
			i -= 32;

		int x0 = (i - 1) * (w - gridStrokeWidth) / getNumBars()
				+ gridStrokeWidth;
		int x1 = i * (w - gridStrokeWidth) / getNumBars();

		int y0 = h - gridStrokeWidth;
		int y1 = (int) (y0 * (1 - Math.min(snr, 60) / 60));

		canvas.drawRect(x0, y1, x1, h, used ? activePaint : inactivePaint);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// don't use Canvas.getWidth() and Canvas.getHeight() here, they may
		// return incorrect values
		int w = getWidth();
		int h = getHeight();

		if (mSats != null) {
			// iterate through list to find out how many bars to draw
			for (GpsSatellite sat : mSats) {
				int prn = sat.getPrn();
				if (prn <= 32) {
					draw32 = true;
				} else if (prn <= 64) {
					Log.wtf(TAG,
							String.format(
									"Got satellite with PRN %d, which should not be happening",
									prn));
					draw64 = true;
				} else if (prn <= 88) {
					draw88 = true;
				} else {
					Log.w(TAG,
							String.format(
									"Got satellite with PRN %d, possibly unsupported system",
									prn));
				}
			}
			// then draw the bars
			for (GpsSatellite sat : mSats) {
				drawSat(canvas, sat.getPrn(), sat.getSnr(), sat.usedInFix());
			}
		}
		// draw the grid on top, with an auxiliary line every 4 sats
		canvas.drawLine((float) gridStrokeWidth / 2, 0,
				(float) gridStrokeWidth / 2, h, gridPaintStrong);
		for (int i = 4; i < getNumBars(); i += 4) {
			float x = (float) gridStrokeWidth / 2 + i * (w - gridStrokeWidth)
					/ getNumBars();
			Paint paint = gridPaint;
			switch (i) {
			case 32:
				if (draw32 || draw64)
					paint = gridPaintStrong;
				break;
			case 56:
				if ((draw32 != draw64) && draw88)
					paint = gridPaintStrong;
				break;
			case 64:
				if (draw32 && draw64)
					paint = gridPaintStrong;
				break;
			case 88:
				if (draw32 && draw64 && draw88)
					paint = gridPaintStrong;
				break;
			default:
				break;
			}
			canvas.drawLine(x, 0, x, h, paint);
		}
		canvas.drawLine(w - (float) gridStrokeWidth / 2, h, w
				- (float) gridStrokeWidth / 2, 0, gridPaintStrong);
		canvas.drawLine(0, h - (float) gridStrokeWidth / 2, w, h
				- (float) gridStrokeWidth / 2, gridPaintStrong);
	}

	public void showSats(Iterable<GpsSatellite> sats) {
		mSats = sats;
		invalidate();
	}
}
