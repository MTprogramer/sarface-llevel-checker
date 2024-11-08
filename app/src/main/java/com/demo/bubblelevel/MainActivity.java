package com.demo.bubblelevel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.bubblelevel.util.Constants;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static final float G_THRESHOLD = 0.97f;
    public static final float MAX_G = 9.8f;
    private static final int MEAN_MEASURES = 10;
    ImageView bubble;
    ImageView bubbleBg;
    ImageButton calibrateButton;
    private int calibratedLoops;
    ImageView hBubble;
    ImageView hBubbleBg;
    TextView hText;

    private double[] mBubbleAngles;
    private double mCalibratedX;
    private double mCalibratedY;
    public float mMaxG;
    private double[] mMeanAngles;
    private double[] mMeanBubbles;
    private int mPrecision;
    private boolean mPremiumAvailable;
    private SensorManager mSensorManager;
    private boolean mShowSign;
    int[] newCoordinates;
    int newLatitude;
    int newLongitude;
    int[] originalCoordinates;
    int originalLatitude;
    int originalLongitude;
    ImageButton pauseButton;
    ProgressBar progressBar;
    ImageButton settingsButton;
    ImageButton shareButton;
    ImageView vBubble;
    ImageView vBubbleBg;
    TextView vText;
    ImageButton visualButton;
    private boolean paused = false;
    float scaleFactor = 0.0f;
    float hScaleFactor = 0.0f;
    float vScaleFactor = 0.0f;
    int mBubbleSensitivity = 5;
    private boolean calibrating = false;

    @Override 
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);


        AdAdmob adAdmob = new AdAdmob( this);
        adAdmob.FullscreenAd_Counter(this);


        this.mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initUI();
        this.mMeanAngles = new double[3];
        this.mMeanBubbles = new double[2];
        this.mBubbleAngles = new double[2];

    }

    private void initUI() {
        this.bubble = (ImageView) findViewById(R.id.imageBubble);
        this.bubbleBg = (ImageView) findViewById(R.id.bubbleBg);
        this.hBubble = (ImageView) findViewById(R.id.hImageBubble);
        this.hBubbleBg = (ImageView) findViewById(R.id.hBubbleBg);
        this.vBubble = (ImageView) findViewById(R.id.vImageBubble);
        this.vBubbleBg = (ImageView) findViewById(R.id.vBubbleBg);
        this.settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        this.calibrateButton = (ImageButton) findViewById(R.id.calibrateButton);
        this.visualButton = (ImageButton) findViewById(R.id.visualButton);
        this.shareButton = (ImageButton) findViewById(R.id.shareButton);
        this.pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.settingsButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        this.calibrateButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                if (MainActivity.this.paused) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setTitle(MainActivity.this.getResources().getString(R.string.calibrate));
                builder.setMessage(R.string.calibrateText);
                builder.setNeutralButton(MainActivity.this.getString(R.string.calibrate), new DialogInterface.OnClickListener() { 
                    @Override 
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.progressBar.setVisibility(View.VISIBLE);
                        MainActivity.this.calibratedLoops = 0;
                        MainActivity.this.mCalibratedX = 0.0d;
                        MainActivity.this.mCalibratedY = 0.0d;
                        MainActivity.this.mMaxG = 0.0f;
                        MainActivity.this.calibrating = true;
                    }
                });
                builder.setNegativeButton(MainActivity.this.getString(R.string.restore), new DialogInterface.OnClickListener() { 
                    @Override 
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        edit.putFloat(Constants.CALIBRATED_X, 0.0f);
                        edit.putFloat(Constants.CALIBRATED_Y, 0.0f);
                        edit.putFloat(Constants.CALIBRATED_G, 9.8f);
                        edit.apply();
                        MainActivity.this.mCalibratedX = 0.0d;
                        MainActivity.this.mCalibratedY = 0.0d;
                        MainActivity.this.mMaxG = 9.8f;
                        MainActivity.this.scaleFactor = 0.0f;
                    }
                });
                builder.setPositiveButton(MainActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() { 
                    @Override 
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }
        });
        if (getPackageManager().hasSystemFeature("android.hardware.camera")) {
            this.visualButton.setVisibility(View.VISIBLE);
            this.visualButton.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View view) {
                    MainActivity.this.startActivity(new Intent(MainActivity.this, VisualActivity.class));
                    MainActivity.this.finish();
                }
            });
        }

        this.shareButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.TEXT", Constants.APP_URL);
                intent.setType("text/plain");
                MainActivity mainActivity = MainActivity.this;
                mainActivity.startActivity(Intent.createChooser(intent, mainActivity.getResources().getText(R.string.share)));
            }
        });
        this.pauseButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                if (!MainActivity.this.paused) {
                    MainActivity.this.pauseButton.setImageResource(R.drawable.play);
                } else {
                    MainActivity.this.pauseButton.setImageResource(R.drawable.pause);
                }
                MainActivity mainActivity = MainActivity.this;
                mainActivity.paused = !mainActivity.paused;
            }
        });
        this.hText = (TextView) findViewById(R.id.hText);
        this.vText = (TextView) findViewById(R.id.vText);
    }

    
    @Override 
    public void onResume() {
        super.onResume();

        loadPreferences();
        double[] dArr = this.mMeanAngles;
        dArr[0] = 0.0d;
        dArr[1] = 0.0d;
        dArr[2] = 0.0d;
        double[] dArr2 = this.mMeanBubbles;
        dArr2[0] = 0.0d;
        dArr2[1] = 0.0d;
        double[] dArr3 = this.mBubbleAngles;
        dArr3[0] = 0.0d;
        dArr3[1] = 0.0d;
        if (this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        SensorManager sensorManager = this.mSensorManager;
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(1), 0);
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
        double degrees;
        double d;
        double degrees2;
        if (this.calibrating) {
            calibrate(sensorEvent.values);
        } else if (this.scaleFactor == 0.0f) {
            int[] iArr = new int[2];
            this.originalCoordinates = iArr;
            iArr[0] = (((int) this.bubbleBg.getX()) + (this.bubbleBg.getWidth() / 2)) - (this.bubble.getWidth() / 2);
            this.originalCoordinates[1] = (((int) this.bubbleBg.getY()) + (this.bubbleBg.getHeight() / 2)) - (this.bubble.getHeight() / 2);
            this.bubble.setX(this.originalCoordinates[0]);
            this.bubble.setY(this.originalCoordinates[1]);
            int x = (((int) this.hBubbleBg.getX()) + (this.hBubbleBg.getWidth() / 2)) - (this.hBubble.getWidth() / 2);
            this.originalLongitude = x;
            this.hBubble.setX(x);
            this.hBubble.setY((((int) this.hBubbleBg.getY()) + (this.hBubbleBg.getHeight() / 2)) - (this.hBubble.getHeight() / 2));
            this.originalLatitude = (((int) this.vBubbleBg.getY()) + (this.vBubbleBg.getHeight() / 2)) - (this.vBubble.getHeight() / 2);
            this.vBubble.setX((((int) this.vBubbleBg.getX()) + (this.vBubbleBg.getWidth() / 2)) - (this.vBubble.getWidth() / 2));
            this.vBubble.setY(this.originalLatitude);
            this.scaleFactor = (this.bubbleBg.getHeight() - this.bubble.getHeight()) / 180.0f;
            this.hScaleFactor = (this.hBubbleBg.getWidth() - this.hBubble.getWidth()) / 180.0f;
            this.vScaleFactor = (this.vBubbleBg.getHeight() - this.vBubble.getHeight()) / 180.0f;
            this.newCoordinates = new int[2];
            System.out.println("V bubble height = " + this.vBubble.getHeight());
            System.out.println("V BG height = " + this.vBubbleBg.getHeight());
        } else if (sensorEvent.sensor.getType() == 1) {
            double sqrt = Math.sqrt(Math.pow(sensorEvent.values[0], 2.0d) + Math.pow(sensorEvent.values[1], 2.0d) + Math.pow(sensorEvent.values[2], 2.0d));
            if (sqrt > this.mMaxG) {
                float[] fArr = sensorEvent.values;
                double d2 = sensorEvent.values[0] * this.mMaxG;
                Double.isNaN(d2);
                fArr[0] = (float) (d2 / sqrt);
                float[] fArr2 = sensorEvent.values;
                double d3 = sensorEvent.values[1] * this.mMaxG;
                Double.isNaN(d3);
                fArr2[1] = (float) (d3 / sqrt);
                float[] fArr3 = sensorEvent.values;
                double d4 = sensorEvent.values[2] * this.mMaxG;
                Double.isNaN(d4);
                fArr3[2] = (float) (d4 / sqrt);
            }
            if (Math.abs(sensorEvent.values[0]) > this.mMaxG * 0.97f) {
                degrees = Math.toDegrees(Math.acos(Math.abs(sensorEvent.values[2] / this.mMaxG)));
                if (this.mMeanAngles[0] < 0.0d) {
                    degrees *= -1.0d;
                }
                d = this.mCalibratedX;
            } else {
                degrees = Math.toDegrees(Math.asin(sensorEvent.values[0] / this.mMaxG));
                d = this.mCalibratedX;
            }
            double d5 = degrees - d;
            if (Math.abs(sensorEvent.values[1]) > this.mMaxG * 0.97f) {
                double degrees3 = Math.toDegrees(Math.acos(Math.abs(sensorEvent.values[2] / this.mMaxG)));
                if (this.mMeanAngles[1] < 0.0d) {
                    degrees3 *= -1.0d;
                }
                degrees2 = degrees3 - this.mCalibratedY;
            } else {
                degrees2 = Math.toDegrees(Math.asin(sensorEvent.values[1] / this.mMaxG)) - this.mCalibratedY;
            }
            double[] dArr = this.mMeanAngles;
            dArr[0] = ((dArr[0] * 9.0d) + d5) / 10.0d;
            dArr[1] = ((dArr[1] * 9.0d) + degrees2) / 10.0d;
            double d6 = this.mBubbleSensitivity;
            Double.isNaN(d6);
            double pow = d6 * (Math.pow(dArr[0], 2.0d) + Math.pow(this.mMeanAngles[1], 2.0d));
            if (pow <= Math.pow(90.0d, 2.0d)) {
                this.mBubbleAngles[0] = this.mMeanAngles[0] * Math.sqrt(this.mBubbleSensitivity);
                this.mBubbleAngles[1] = this.mMeanAngles[1] * Math.sqrt(this.mBubbleSensitivity);
            } else {
                double sqrt2 = Math.sqrt(Math.pow(90.0d, 2.0d) / pow);
                this.mBubbleAngles[0] = this.mMeanAngles[0] * Math.sqrt(this.mBubbleSensitivity) * sqrt2;
                this.mBubbleAngles[1] = this.mMeanAngles[1] * Math.sqrt(this.mBubbleSensitivity) * sqrt2;
            }
            double[] dArr2 = this.mMeanBubbles;
            double[] dArr3 = this.mBubbleAngles;
            dArr2[0] = ((dArr2[0] * 9.0d) + dArr3[0]) / 10.0d;
            dArr2[1] = ((dArr2[1] * 9.0d) + dArr3[1]) / 10.0d;
            int[] iArr2 = this.newCoordinates;
            int[] iArr3 = this.originalCoordinates;
            double d7 = iArr3[0];
            double d8 = dArr2[0];
            float f = this.scaleFactor;
            double d9 = f;
            Double.isNaN(d9);
            Double.isNaN(d7);
            iArr2[0] = (int) (d7 + (d8 * d9));
            double d10 = iArr3[1];
            double d11 = dArr2[1];
            double d12 = f;
            Double.isNaN(d12);
            Double.isNaN(d10);
            iArr2[1] = (int) (d10 - (d11 * d12));
            if (this.paused) {
                return;
            }
            this.bubble.setX(iArr2[0]);
            this.bubble.setY(this.newCoordinates[1]);
            double d13 = this.originalLongitude;
            double d14 = this.mMeanBubbles[0];
            double d15 = this.hScaleFactor;
            Double.isNaN(d15);
            Double.isNaN(d13);
            int i = (int) (d13 + (d14 * d15));
            this.newLongitude = i;
            this.hBubble.setX(i);
            double d16 = this.originalLatitude;
            double d17 = this.mMeanBubbles[1];
            double d18 = this.vScaleFactor;
            Double.isNaN(d18);
            Double.isNaN(d16);
            int i2 = (int) (d16 - (d17 * d18));
            this.newLatitude = i2;
            this.vBubble.setY(i2);
            if (this.mShowSign) {
                this.hText.setText(String.format("%." + this.mPrecision + "f º", Double.valueOf(this.mMeanAngles[0])));
                this.vText.setText(String.format("%." + this.mPrecision + "f º", Double.valueOf(this.mMeanAngles[1])));
            } else {
                this.hText.setText(String.format("%." + this.mPrecision + "f º", Double.valueOf(Math.abs(this.mMeanAngles[0]))));
                this.vText.setText(String.format("%." + this.mPrecision + "f º", Double.valueOf(Math.abs(this.mMeanAngles[1]))));
            }
            if (Math.abs(this.mMeanAngles[0]) > 85.0d) {
                this.hText.setTextColor(getResources().getColor(R.color.red));
            } else {
                this.hText.setTextColor(getResources().getColor(R.color.degrees));
            }
            if (Math.abs(this.mMeanAngles[1]) > 85.0d) {
                this.vText.setTextColor(getResources().getColor(R.color.red));
            } else {
                this.vText.setTextColor(getResources().getColor(R.color.degrees));
            }
        }
    }

    private void calibrate(float[] fArr) {
        int i = this.calibratedLoops;
        if (i < 30) {
            this.calibratedLoops = i + 1;
        } else if (i < 60) {
            double d = this.mMaxG * i;
            double sqrt = Math.sqrt(Math.pow(fArr[0], 2.0d) + Math.pow(fArr[1], 2.0d) + Math.pow(fArr[2], 2.0d));
            Double.isNaN(d);
            double d2 = d + sqrt;
            int i2 = this.calibratedLoops;
            double d3 = i2 + 1;
            Double.isNaN(d3);
            this.mMaxG = (float) (d2 / d3);
            this.calibratedLoops = i2 + 1;
        } else {
            double sqrt2 = Math.sqrt(Math.pow(fArr[0], 2.0d) + Math.pow(fArr[1], 2.0d));
            float[] fArr2 = new float[2];
            float f = this.mMaxG;
            if (sqrt2 > f) {
                double d4 = fArr[0] * f;
                Double.isNaN(d4);
                fArr2[0] = (float) (d4 / sqrt2);
                double d5 = fArr[1] * f;
                Double.isNaN(d5);
                fArr2[1] = (float) (d5 / sqrt2);
            } else {
                fArr2[0] = fArr[0];
                fArr2[1] = fArr[1];
            }
            double d6 = this.mCalibratedX;
            double d7 = this.calibratedLoops;
            Double.isNaN(d7);
            double degrees = (d6 * d7) + Math.toDegrees(Math.asin(fArr2[0] / this.mMaxG));
            int i3 = this.calibratedLoops;
            double d8 = i3;
            Double.isNaN(d8);
            this.mCalibratedX = degrees / (d8 + 1.0d);
            double d9 = this.mCalibratedY;
            double d10 = i3;
            Double.isNaN(d10);
            double degrees2 = (d9 * d10) + Math.toDegrees(Math.asin(fArr2[1] / this.mMaxG));
            int i4 = this.calibratedLoops;
            double d11 = i4;
            Double.isNaN(d11);
            this.mCalibratedY = degrees2 / (d11 + 1.0d);
            int i5 = i4 + 1;
            this.calibratedLoops = i5;
            if (i5 > 110) {
                System.out.println("Calibrated values = " + this.mMaxG + " | " + this.mCalibratedX + " , " + this.mCalibratedY);
                this.calibrating = false;
                this.scaleFactor = 0.0f;
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
                edit.putFloat(Constants.CALIBRATED_X, (float) this.mCalibratedX);
                edit.putFloat(Constants.CALIBRATED_Y, (float) this.mCalibratedY);
                edit.putFloat(Constants.CALIBRATED_G, this.mMaxG);
                edit.apply();
                this.progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, getResources().getString(R.string.calibration_finished), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPreferences() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.mCalibratedX = defaultSharedPreferences.getFloat(Constants.CALIBRATED_X, 0.0f);
        this.mCalibratedY = defaultSharedPreferences.getFloat(Constants.CALIBRATED_Y, 0.0f);
        this.mMaxG = defaultSharedPreferences.getFloat(Constants.CALIBRATED_G, 9.8f);
        this.mShowSign = defaultSharedPreferences.getBoolean(Constants.SHOW_SIGN, true);
        this.mPrecision = defaultSharedPreferences.getInt(Constants.PRECISION, 1);
        int i = defaultSharedPreferences.getInt(Constants.BUBBLE_SENSITIVITY, 0);
        if (i == 1) {
            this.mBubbleSensitivity = 5;
        } else if (i == 2) {
            this.mBubbleSensitivity = 10;
        } else if (i == 3) {
            this.mBubbleSensitivity = 25;
        } else {
            this.mBubbleSensitivity = 2;
        }
        int i2 = R.mipmap.greenbubble;
        int i3 = defaultSharedPreferences.getInt(Constants.BUBBLE_COLOR, 0);
        if (i3 == 1) {
            i2 = R.mipmap.bluebubble;
        } else if (i3 == 2) {
            i2 = R.mipmap.redbubble;
        } else if (i3 == 3) {
            i2 = R.mipmap.blackbubble;
        }
        this.bubble.setImageDrawable(getResources().getDrawable(i2));
        this.vBubble.setImageDrawable(getResources().getDrawable(i2));
        this.hBubble.setImageDrawable(getResources().getDrawable(i2));
    }
}
