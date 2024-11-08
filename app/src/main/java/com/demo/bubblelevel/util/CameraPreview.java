package com.demo.bubblelevel.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback, Camera.AutoFocusCallback {
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private static final int LANDSCAPE_TO_PORTRAIT = 90;
    private static final int PORTRAIT_TO_LANDSCAPE = 0;
    private boolean autoFocus;
    private Camera mCamera;
    SurfaceHolder mHolder;
    private Point mPreviewSize;
    private List<Camera.Size> mSupportedPreviewSizes;
    SurfaceView mSurfaceView;
    private Activity mainActivity;

    @Override 
    public void onAutoFocus(boolean z, Camera camera) {
    }

    @Override 
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
    }

    @Override 
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    public CameraPreview(Context context, SurfaceView surfaceView, Point point) {
        super(context);
        this.autoFocus = false;
        this.mainActivity = (Activity) context;
        this.mSurfaceView = surfaceView;
        this.mPreviewSize = new Point(point.x, point.y);
        SurfaceHolder holder = this.mSurfaceView.getHolder();
        this.mHolder = holder;
        holder.addCallback(this);
        this.mHolder.setType(3);
    }

    private void setCameraDisplayOrientation(Activity activity, int i) {
        int i2;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(i, cameraInfo);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int i3 = 0;
        if (rotation != 0) {
            if (rotation == 1) {
                i3 = 90;
            } else if (rotation == 2) {
                i3 = 180;
            } else if (rotation == 3) {
                i3 = 270;
            }
        }
        if (cameraInfo.facing == 1) {
            i2 = (360 - ((cameraInfo.orientation + i3) % 360)) % 360;
        } else {
            i2 = ((cameraInfo.orientation - i3) + 360) % 360;
        }
        this.mCamera.setDisplayOrientation(i2);
    }

    public void turnFlashOn() {
        Camera.Parameters parameters = this.mCamera.getParameters();
        parameters.setFlashMode("torch");
        this.mCamera.setParameters(parameters);
    }

    public void turnFlashOff() {
        Camera.Parameters parameters = this.mCamera.getParameters();
        parameters.setFlashMode("off");
        this.mCamera.setParameters(parameters);
    }

    public boolean isAutofocusSupported() {
        return this.autoFocus;
    }

    @Override 
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (this.mCamera == null) {
            this.mCamera = Camera.open(0);
        }
        Camera camera = this.mCamera;
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getSupportedFocusModes().contains("continuous-picture")) {
                this.autoFocus = false;
                parameters.setFocusMode("continuous-picture");
            } else if (parameters.getSupportedFocusModes().contains("auto")) {
                this.autoFocus = true;
                parameters.setFocusMode("auto");
            }
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            this.mSupportedPreviewSizes = supportedPreviewSizes;
            for (Camera.Size size : supportedPreviewSizes) {
                PrintStream printStream = System.out;
                printStream.println(size.width + " x " + size.height);
            }
            this.mPreviewSize = getOptimalPreviewSize(this.mSupportedPreviewSizes, i2, i3);
            PrintStream printStream2 = System.out;
            printStream2.println("Preview size = " + this.mPreviewSize.x + " X " + this.mPreviewSize.y);
            if (this.mPreviewSize.x > this.mPreviewSize.y) {
                parameters.setPreviewSize(this.mPreviewSize.x, this.mPreviewSize.y);
            } else {
                parameters.setPreviewSize(this.mPreviewSize.y, this.mPreviewSize.x);
            }
            this.mCamera.setParameters(parameters);
            PrintStream printStream3 = System.out;
            printStream3.println("Width = " + i2 + " | Height = " + i3);
            PrintStream printStream4 = System.out;
            printStream4.println("Surface Width = " + this.mSurfaceView.getWidth() + " | Surface Height = " + this.mSurfaceView.getHeight());
            float f = ((float) this.mPreviewSize.x) / ((float) this.mPreviewSize.y);
            float f2 = ((float) i3) / ((float) i2);
            PrintStream printStream5 = System.out;
            printStream5.println("previewRatio = " + f + " | SurfaceRatio = " + f2);
            if (f > f2) {
                this.mSurfaceView.setScaleY(f / f2);
            } else {
                this.mSurfaceView.setScaleX(f2 / f);
            }
            requestLayout();
            try {
                this.mCamera.setPreviewDisplay(this.mHolder);
            } catch (IOException unused) {
            }
            setCameraDisplayOrientation(this.mainActivity, 0);
            this.mCamera.startPreview();
        }
    }

    @Override 
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    @Override 
    protected void onMeasure(int i, int i2) {
        float f;
        int i3;
        System.out.println("onMeasure");
        int resolveSize = resolveSize(getSuggestedMinimumWidth(), i);
        int resolveSize2 = resolveSize(getSuggestedMinimumHeight(), i2);
        List<Camera.Size> list = this.mSupportedPreviewSizes;
        if (list != null) {
            this.mPreviewSize = getOptimalPreviewSize(list, resolveSize, resolveSize2);
        }
        Point point = this.mPreviewSize;
        if (point != null) {
            if (point.y >= this.mPreviewSize.x) {
                f = this.mPreviewSize.y;
                i3 = this.mPreviewSize.x;
            } else {
                f = this.mPreviewSize.y;
                i3 = this.mPreviewSize.x;
            }
            setMeasuredDimension(resolveSize, (int) (resolveSize * (f / i3)));
        }
    }

    private Point getOptimalPreviewSize(List<Camera.Size> list, int i, int i2) {
        double d = i2;
        double d2 = i;
        Double.isNaN(d);
        Double.isNaN(d2);
        double d3 = d / d2;
        Point point = null;
        if (list == null) {
            return null;
        }
        double d4 = Double.MAX_VALUE;
        double d5 = Double.MAX_VALUE;
        for (Camera.Size size : list) {
            double d6 = size.height;
            double d7 = size.width;
            Double.isNaN(d6);
            Double.isNaN(d7);
            if (Math.abs((d6 / d7) - d3) <= 0.9d && Math.abs(size.height - i2) < d5) {
                point = new Point(size.width, size.height);
                d5 = Math.abs(size.height - i2);
            }
        }
        if (point == null) {
            for (Camera.Size size2 : list) {
                if (Math.abs(size2.height - i2) < d4) {
                    point = new Point(size2.width, size2.height);
                    d4 = Math.abs(size2.height - i2);
                }
            }
        }
        return point;
    }

    public void onActivityPaused() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.stopPreview();
            this.mCamera.release();
        }
    }

    public void pausePreview() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.stopPreview();
        }
    }

    public void resumePreview() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.startPreview();
        }
    }
}
