package com.grace.covid;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

// https://stackoverflow.com/questions/10630373/android-image-view-pinch-zooming
public class PdfViewerActivity extends AppCompatActivity {

    private static final String TAG = PdfViewerActivity.class.getName();

    File file;
    Bitmap bitmap;
    ImageView imageView;
    ScaleGestureDetector gestureDetector;
    Matrix matrix;
    Matrix matrixSave;

    // Different state
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        file = new File(getApplicationContext().getExternalFilesDir(
                "attestation") + "/Attestation_de_deplacement_derogatoire.pdf");

        matrix = new Matrix();
        matrixSave = new Matrix();

        imageView = findViewById(R.id.pdf_view);

        readPDF(file);
    }

    private void dumpEvent(MotionEvent event) {
        String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder stringBuilder = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        stringBuilder.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            stringBuilder.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            stringBuilder.append(")");
        }
        stringBuilder.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            stringBuilder.append("#").append(i);
            stringBuilder.append("(pid ").append(event.getPointerId(i));
            stringBuilder.append(")=").append((int) event.getX(i));
            stringBuilder.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount()) {
                stringBuilder.append(";");
            }
            stringBuilder.append("]");
            Log.d(TAG, stringBuilder.toString());
        }
    }

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //gestureDetector.onTouchEvent(event);

        dumpEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                matrixSave.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    matrixSave.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(matrixSave);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 100f) {
                        matrix.set(matrixSave);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        imageView.setImageMatrix(matrix);
        return true;
    }

    private void readPDF(File file) {
        if (file.exists()) {
            // Instantiating the PDFRenderer class
            PdfRenderer renderer = null;
            try {
                renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Rendering an image from the PDF document
            assert renderer != null;
            PdfRenderer.Page page = renderer.openPage(0);
            //bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            //canvas.saveLayer(new RectF(45, 45, 355, 355), paint);

            ///bitmap = Bitmap.createBitmap(bitmap, 0, 0, 400, 400);
            imageView.setImageBitmap(bitmap);

            // Closing the document
            page.close();
            renderer.close();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}


/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }*/