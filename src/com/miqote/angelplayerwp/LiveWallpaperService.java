/*
* Copyright (C) 2014 Yonnji Nyyoka, yonnji@miqote.com
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*/
package com.miqote.angelplayerwp;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class LiveWallpaperService extends WallpaperService {

    public static final int BACKGROUND_LIGHT = 0xff2a64c8;
    public static final int BACKGROUND_DARK = 0xff1f387a;
    public static final int LABEL_LIGHT = 0xfffcfaff;
    public static final int LABEL_DARK = 0xffb9bdfc;
    public static final int LABEL_SHADOW = 0x886b81ca;
    public static final int SUMMARY = 0xffffffff;
    public static final int[] CIRCLES_COLOR = {0xff3eade2, 0xff5aa4ff, 0xff61ecff};

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new MainEngine();
    }

    public class MainEngine extends Engine implements Runnable,
            SharedPreferences.OnSharedPreferenceChangeListener {

        private Handler handler = new Handler();
        private boolean isVisible = true;
        private float alpha = 0;
        private SharedPreferences preferences;
        private boolean antialiasing = false;
        private boolean rotate = false;
        private boolean text = true;
        private Paint background = null;
        private Circle[] circles = null;
        private Paint label = null;
        private Paint labelShadow = null;
        private Paint summary = null;
        private long total = Runtime.getRuntime().totalMemory() / 1024;
        private long free = Runtime.getRuntime().freeMemory() / 1024;

        public MainEngine() {
            preferences = LiveWallpaperService.this.getSharedPreferences("settings", 0);
            preferences.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(preferences, null);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(this);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            isVisible = visible;
            if (isVisible) {
                run();
            } else {
                handler.removeCallbacks(this);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            isVisible = false;
            handler.removeCallbacks(this);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
        }

        @Override
        public final void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            antialiasing = prefs.getBoolean("antialiasing", false);
            rotate = prefs.getBoolean("rotate", false);
            text = prefs.getBoolean("text", true);
            background = null;
            circles = null;
            label = null;
            labelShadow = null;
        }

        private void doDraw(Canvas canvas) {
            Rect bounds = new Rect(0, 0, canvas.getWidth(), canvas.getHeight()); // []
            if (canvas.getHeight() <= canvas.getWidth()) { // [  ]
                bounds = new Rect(0, 0, canvas.getHeight(), canvas.getWidth());
            }
            if (background == null) {
                LinearGradient gradient = new LinearGradient(bounds.left, bounds.top,
                        bounds.right, bounds.top, BACKGROUND_DARK, BACKGROUND_LIGHT,
                        Shader.TileMode.CLAMP);
                background = new Paint();
                background.setDither(true);
                background.setShader(gradient);
            }
            if (circles == null) {
                circles = new Circle[CIRCLES_COLOR.length];
                // circle 1
                float r = bounds.height() / 2;
                float x = bounds.width() / 2;
                float y = bounds.top;
                Paint p = new Paint();
                p.setAntiAlias(antialiasing);
                p.setColor(CIRCLES_COLOR[0]);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(r / 100);
                circles[0] = new Circle(x, y, r, p);
                // circle 2
                r = bounds.width() / 2;
                x = bounds.left;
                y = bounds.bottom;
                p = new Paint();
                p.setAntiAlias(antialiasing);
                p.setColor(CIRCLES_COLOR[1]);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(r / 100);
                circles[1] = new Circle(x, y, r, p);
                // circle 3
                r = bounds.height() / 2;
                x = bounds.width() / 2 + r * 1.1f;
                y = bounds.top;
                p = new Paint();
                p.setAntiAlias(antialiasing);
                p.setColor(CIRCLES_COLOR[2]);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(r / 100);
                circles[2] = new Circle(x, y, r, p);
            }
            if (label == null) {
                float x = bounds.right - bounds.width() / 3;
                float y = bounds.top + bounds.height() / 3;
                label = new Paint();
                label.setAntiAlias(antialiasing);
                label.setTextSize(bounds.height() / 15);
                LinearGradient gradient = new LinearGradient(x, y, x, y - label.getTextSize(),
                        LABEL_DARK, LABEL_LIGHT, Shader.TileMode.CLAMP);
                label.setShader(gradient);
            }
            if (labelShadow == null) {
                labelShadow = new Paint();
                labelShadow.setAntiAlias(antialiasing);
                labelShadow.setColor(LABEL_SHADOW);
                labelShadow.setTextSize(label.getTextSize());
            }
            if (summary == null) {
                summary = new Paint();
                summary.setAntiAlias(antialiasing);
                summary.setColor(SUMMARY);
                summary.setTextSize(label.getTextSize() / 3);
            }
            alpha -= 0.5f;
            if (alpha >= 360 || alpha <= -360) {
                alpha = 0;
            }
            if (alpha % 30 == 0) {
                free = Runtime.getRuntime().freeMemory() / 1024;
            }
            if (canvas.getHeight() <= canvas.getWidth()) { // [  ]
                canvas.save();
                canvas.rotate(-90, bounds.width() / 2, bounds.width() / 2);
            }
            canvas.save();
            if (rotate) {
                canvas.rotate(180, bounds.width() / 2, bounds.height() / 2);
            }
            canvas.drawRect(bounds, background);
            circles[0].onDraw(canvas, alpha);
            circles[1].onDraw(canvas, alpha);
            circles[2].onDraw(canvas, -alpha);
            if (text) {
                float x = bounds.right - bounds.width() / 3;
                float y = bounds.top + bounds.height() / 3;
                canvas.save();
                canvas.rotate(90, x, y);
                canvas.drawText("A N G E L  P L A Y E R ", x + 3, y + 3, labelShadow);
                canvas.drawText("A N G E L  P L A Y E R ", x, y, label);
                y += summary.getTextSize();
                canvas.drawText("ver.:1.0", x, y, summary);
                y += summary.getTextSize();
                canvas.drawText(total + "Kb total; " + free + "Kb free", x, y, summary);
                canvas.restore();
            }
            if (canvas.getHeight() <= canvas.getWidth()) { // [  ]
                canvas.restore();
            }
        }

        @Override
        public final void run() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    doDraw(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            handler.removeCallbacks(this);
            if (isVisible) {
                handler.postDelayed(this, 1000 / 30);
            }
        }
    }
}
