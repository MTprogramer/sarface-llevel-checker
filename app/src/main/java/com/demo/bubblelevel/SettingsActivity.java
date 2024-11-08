package com.demo.bubblelevel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;

import com.demo.bubblelevel.util.Constants;


public class SettingsActivity extends AppCompatActivity {
    ImageButton backButton;
    Spinner colorSpinner;
    Spinner precisionSpinner;
    AppCompatTextView restoreText;
    AppCompatButton saveButton;
    Spinner sensitivitySpinner;
    SwitchCompat signSwitch;

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);



        AdAdmob adAdmob = new AdAdmob( this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.banner), this);
        adAdmob.FullscreenAd_Counter(this);

        initUI();
    }

    private void initUI() {
        this.backButton = (ImageButton) findViewById(R.id.backButton);
        this.precisionSpinner = (Spinner) findViewById(R.id.precisionSpinner);
        this.colorSpinner = (Spinner) findViewById(R.id.bubbleColorSpinner);
        this.sensitivitySpinner = (Spinner) findViewById(R.id.bubbleSensitivitySpinner);
        this.signSwitch = (SwitchCompat) findViewById(R.id.signSwitch);
        this.saveButton = (AppCompatButton) findViewById(R.id.saveButton);
        this.restoreText = (AppCompatTextView) findViewById(R.id.restoreText);
        ArrayAdapter<CharSequence> createFromResource = ArrayAdapter.createFromResource(this, R.array.precision_options, R.layout.simple_spinner_item);
        createFromResource.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        this.precisionSpinner.setAdapter((SpinnerAdapter) createFromResource);
        ArrayAdapter<CharSequence> createFromResource2 = ArrayAdapter.createFromResource(this, R.array.bubble_colors, R.layout.simple_spinner_item);
        createFromResource2.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        this.colorSpinner.setAdapter((SpinnerAdapter) createFromResource2);
        ArrayAdapter<CharSequence> createFromResource3 = ArrayAdapter.createFromResource(this, R.array.sensitivity_options, R.layout.simple_spinner_item);
        createFromResource3.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        this.sensitivitySpinner.setAdapter((SpinnerAdapter) createFromResource3);
        this.backButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                SettingsActivity.this.finish();
            }
        });
        this.saveButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                SettingsActivity.this.saveSettings();
            }
        });
        this.restoreText.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                SettingsActivity.this.restoreFactorySettings();
            }
        });
        loadSettings();
    }

    
    public void saveSettings() {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
        edit.putBoolean(Constants.SHOW_SIGN, this.signSwitch.isChecked());
        edit.putInt(Constants.PRECISION, this.precisionSpinner.getSelectedItemPosition());
        edit.putInt(Constants.BUBBLE_COLOR, this.colorSpinner.getSelectedItemPosition());
        edit.putInt(Constants.BUBBLE_SENSITIVITY, this.sensitivitySpinner.getSelectedItemPosition());
        edit.apply();
        Toast.makeText(this, getResources().getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
    }

    private void loadSettings() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.signSwitch.setChecked(defaultSharedPreferences.getBoolean(Constants.SHOW_SIGN, true));
        this.precisionSpinner.setSelection(defaultSharedPreferences.getInt(Constants.PRECISION, 1), true);
        this.colorSpinner.setSelection(defaultSharedPreferences.getInt(Constants.BUBBLE_COLOR, 0), true);
        this.sensitivitySpinner.setSelection(defaultSharedPreferences.getInt(Constants.BUBBLE_SENSITIVITY, 0), true);
    }

    
    public void restoreFactorySettings() {
        this.signSwitch.setChecked(true);
        this.precisionSpinner.setSelection(1, true);
        this.colorSpinner.setSelection(0, true);
        this.sensitivitySpinner.setSelection(0, true);
        saveSettings();
    }
}
