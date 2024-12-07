/*
* Copyright (C) 2014 Yonnji Nyyoka, yonnji@kitsune.one
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
package one.kitsune.angelplayerwp;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class Circle {

    private static final float[] SAMPLE_R = {
        520f * 1.3846154f,
        520f * 1.2884615f,
        520f * 1.2307692f,
        520f * 1.1923077f,
        520f * 1.1153846f,
        520f,
        520f * 0.92307692f,
        520f * 0.884615382f,
        520f * 0.84615384f,
        520f * 0.80769231f,
        320f,
        520f * 0.5576923f,
        520f * 0.51923076f,
        520f * 0.42307692f,
        520f * 0.32692308f,
        105f
    };
    private static final int[] RV = {0, 2, 3, 5, 6, 10, 15}; // visible circles
    private static float[] RK;
    private float[] R;
    private float x;
    private float y;
    private float a = 0;
    private Paint paint;
    private Paint bigText;
    private Paint smallText;
    private Path smallPath;
    private Path bigPath;

    static {
        // C1: 100% -- SAMPLE_R1
        // C2: RK% -- SAMPLE_R2
        // RK = SAMPLE_R2 * 100 / SAMPLE_R1
        RK = new float[SAMPLE_R.length];
        for (int i = 0; i < RK.length; i++) {
            RK[i] = SAMPLE_R[i] * 100f / SAMPLE_R[0];
        }
    }

    public Circle(float x, float y, float r, Paint paint) {
        this.x = x;
        this.y = y;
        this.paint = paint;
        // C1: 100% -- r
        // C2: RK% -- r2
        // r2 = RK * r / 100
        R = new float[RK.length];
        for (int i = 0; i < R.length; i++) {
            R[i] = RK[i] * r / 100f;
        }
        bigText = new Paint();
        bigText.setAntiAlias(paint.isAntiAlias());
        bigText.setColor(paint.getColor());
        bigText.setTextAlign(Paint.Align.CENTER);
        bigText.setTextSize(R[0] / 9);
        smallText = new Paint();
        smallText.setAntiAlias(paint.isAntiAlias());
        smallText.setColor(paint.getColor());
        smallText.setTextAlign(Paint.Align.CENTER);
        smallText.setTextSize(R[0] / 18);
        smallPath = new Path();
        smallPath.addCircle(x, y, R[5], Path.Direction.CW);
        bigPath = new Path();
        bigPath.addCircle(x, y, R[10], Path.Direction.CW);
    }

    public void onDraw(Canvas c, float a) {
        for (int i : RV) {
            c.drawCircle(x, y, R[i], paint);
        }
        for (float i = 0; i < 360; i += 5f) {
            drawLine(c, x, y, R[0], R[1], i + a + 2.5f, paint);
            drawLine(c, x, y, R[6], R[7], i + a + 2.5f, paint);
            if (i % 5 == 0) {
                if (i % 15 != 0) {
                    drawLine(c, x, y, R[0], R[2], i + a, paint);
                    drawLine(c, x, y, R[4], R[8], i + a, paint);
                    drawLine(c, x, y, R[10], R[11], i + a, paint);
                    drawLine(c, x, y, R[14], R[15], i + a, paint);
                }
            }
            if (i % 15 == 0) {
                drawLine(c, x, y, R[10], R[12], i + a, paint);
                drawLine(c, x, y, R[13], R[15], i + a, paint);
            }
            if (i % 30 == 0) {
                drawLine(c, x, y, R[0], R[3], i + a + 15f, paint);
                drawLine(c, x, y, R[4], R[9], i + a + 15f, paint);
                drawLine(c, x, y, R[0], R[4], i + a, paint);
                drawLine(c, x, y, R[5], R[9], i + a, paint);
            }
            if (i % 90 == 0) {
                drawLine(c, x, y, 0, R[15], i + a, paint);
            }
        }
        c.save();
        c.rotate(a, x, y);
        for (float i = 0; i < 360; i += 30) {
            c.rotate(30, x, y);
            c.drawTextOnPath(String.valueOf(Math.round(i)), smallPath,
                    0, -smallText.getTextSize() / 3, smallText);
            c.drawTextOnPath(String.valueOf(Math.round(i)), bigPath,
                    0, -bigText.getTextSize() / 3, bigText);
        }
        c.restore();
    }

    private void drawLine(Canvas c, float x, float y, float r1, float r2, float a, Paint paint) {
        float x1 = (float) (x + Math.cos(Math.toRadians(a)) * r1);
        float y1 = (float) (y + Math.sin(Math.toRadians(a)) * r1);
        float x2 = (float) (x + Math.cos(Math.toRadians(a)) * r2);
        float y2 = (float) (y + Math.sin(Math.toRadians(a)) * r2);
        c.drawLine(x1, y1, x2, y2, paint);
    }
}
