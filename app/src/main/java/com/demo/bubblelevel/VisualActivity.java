package com.demo.bubblelevel;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.demo.bubblelevel.util.CameraPreview;
import com.demo.bubblelevel.util.Constants;


public class VisualActivity extends AppCompatActivity implements SensorEventListener {
    private static final int CAMERA_REQUEST = 0;
    public static final float G_THRESHOLD = 0.97f;
    public static final float MAX_G = 9.8f;
    private static final int MEAN_MEASURES = 25;
    private ImageView backgroundView;
    private int calibratedLoops;
    ImageButton classicButton;
    TextView degreesText;
    private ImageView mGreenCross;
    public float mMaxG;
    private double[] mMeanEvents;
    private int mPrecision;
    private CameraPreview mPreview;
    private SensorManager mSensorManager;
    ImageButton pauseButton;
    SurfaceView pictureView;
    ProgressBar progressBar;
    ImageButton settingsButton;
    ImageButton shareButton;
    private boolean paused = false;
    private boolean requestedPermissions = false;

    @Override 
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_visual);


        AdAdmob adAdmob = new AdAdmob( this);
        adAdmob.FullscreenAd_Counter(this);

        this.mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initUI();
        this.mMeanEvents = new double[3];
    }

    private void initUI() {
        this.settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        this.classicButton = (ImageButton) findViewById(R.id.classicButton);
        this.shareButton = (ImageButton) findViewById(R.id.shareButton);
        this.pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        this.pictureView = (SurfaceView) findViewById(R.id.surfaceView);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.mGreenCross = (ImageView) findViewById(R.id.greenCross);
        this.settingsButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                VisualActivity.this.startActivity(new Intent(VisualActivity.this, SettingsActivity.class));
            }
        });
        this.classicButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                VisualActivity.this.startActivity(new Intent(VisualActivity.this, MainActivity.class));
                VisualActivity.this.finish();
            }
        });

        this.shareButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.TEXT", Constants.APP_URL);
                intent.setType("text/plain");
                VisualActivity visualActivity = VisualActivity.this;
                visualActivity.startActivity(Intent.createChooser(intent, visualActivity.getResources().getText(R.string.share)));
            }
        });
        this.pauseButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                if (VisualActivity.this.mPreview == null) {
                    return;
                }
                if (!VisualActivity.this.paused) {
                    VisualActivity.this.pauseButton.setImageResource(R.drawable.play);
                    VisualActivity.this.mPreview.pausePreview();
                } else {
                    VisualActivity.this.pauseButton.setImageResource(R.drawable.pause);
                    VisualActivity.this.mPreview.resumePreview();
                }
                VisualActivity visualActivity = VisualActivity.this;
                visualActivity.paused = !visualActivity.paused;
            }
        });
        this.degreesText = (TextView) findViewById(R.id.degreesText);
    }

    public  void lambda$initUI$0$VisualActivity(View view) {
        try {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://developer?id=RRM")));
        } catch (ActivityNotFoundException unused) {
            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/developer?id=RRM")));
        }
    }

    @Override 
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 0) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0) {
                startActivity(new Intent(this, VisualActivity.class));
                finish();
                return;
            }
            Toast.makeText(this, getResources().getString(R.string.permission_explanation), Toast.LENGTH_LONG).show();
        }
    }

    
    @Override 
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0) {
            initCamera();
        } else if (!this.requestedPermissions) {
            this.requestedPermissions = true;
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, 0);
        }

        loadPreferences();
        double[] dArr = this.mMeanEvents;
        dArr[0] = 0.0d;
        dArr[1] = 0.0d;
        dArr[2] = 0.0d;
        if (this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(1), 0);
        }
        if (this.paused) {
            this.paused = false;
            this.pauseButton.setImageResource(R.drawable.pause);
        }
    }

    
    @Override 
    public void onPause() {
        this.mSensorManager.unregisterListener(this);
        super.onPause();
    }

    
    @Override 
    public void onStop() {
        this.mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override 
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == 1) {
            double sqrt = Math.sqrt(Math.pow(sensorEvent.values[0], 2.0d) + Math.pow(sensorEvent.values[1], 2.0d) + Math.pow(sensorEvent.values[2], 2.0d));
            if (sqrt > this.mMaxG) {
                float[] fArr = sensorEvent.values;
                double d = sensorEvent.values[0] * this.mMaxG;
                Double.isNaN(d);
                fArr[0] = (float) (d / sqrt);
                float[] fArr2 = sensorEvent.values;
                double d2 = sensorEvent.values[1] * this.mMaxG;
                Double.isNaN(d2);
                fArr2[1] = (float) (d2 / sqrt);
                float[] fArr3 = sensorEvent.values;
                double d3 = sensorEvent.values[2] * this.mMaxG;
                Double.isNaN(d3);
                fArr3[2] = (float) (d3 / sqrt);
            }
            if (this.paused) {
                return;
            }
            double[] dArr = this.mMeanEvents;
            double d4 = sensorEvent.values[0];
            Double.isNaN(d4);
            dArr[0] = ((dArr[0] * 24.0d) + d4) / 25.0d;
            double[] dArr2 = this.mMeanEvents;
            double d5 = sensorEvent.values[1];
            Double.isNaN(d5);
            dArr2[1] = ((dArr2[1] * 24.0d) + d5) / 25.0d;
            double[] dArr3 = this.mMeanEvents;
            double degrees = Math.toDegrees(Math.atan(Math.abs(dArr3[0] / dArr3[1])));
            this.degreesText.setText(String.format("%." + this.mPrecision + "f º", Double.valueOf(degrees)));
            if (this.mMeanEvents[0] < 0.0d) {
                degrees *= -1.0d;
            }
            if (this.mMeanEvents[1] < 0.0d) {
                degrees = 180.0d - degrees;
            }
            this.mGreenCross.setRotation((float) degrees);
        }
    }

    private void loadPreferences() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.mMaxG = defaultSharedPreferences.getFloat(Constants.CALIBRATED_G, 9.8f);
        this.mPrecision = defaultSharedPreferences.getInt(Constants.PRECISION, 1);
    }

    private void initCamera() {
        if (this.mPreview == null) {
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            defaultDisplay.getSize(point);
            this.mPreview = new CameraPreview(this, this.pictureView, point);
        }
    }
}
