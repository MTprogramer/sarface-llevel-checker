package com.demo.bubblelevel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.demo.bubblelevel.util.Constants;


public class MenuActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageButton shareButton;
    private AppCompatButton startButton;
    private Thread waitThread;


    
    @Override
    
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_menu);


        AdAdmob adAdmob = new AdAdmob( this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.banner), this);
        adAdmob.FullscreenAd_Counter(this);


        this.startButton = (AppCompatButton) findViewById(R.id.startButton);

        this.shareButton = (ImageButton) findViewById(R.id.shareButton);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        this.startButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                MenuActivity.this.startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });

        this.shareButton.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.TEXT", Constants.APP_URL);
                intent.setType("text/plain");
                MenuActivity menuActivity = MenuActivity.this;
                menuActivity.startActivity(Intent.createChooser(intent, menuActivity.getResources().getText(R.string.share)));
            }
        });
    }


    
    @Override 
    public void onPause() {
        Thread thread = this.waitThread;
        if (thread != null) {
            thread.interrupt();
        }

        super.onPause();
    }

    
    @Override 
    public void onResume() {
        this.startButton.setEnabled(true);
        this.shareButton.setEnabled(true);

        toggleStartButton(true);

        super.onResume();
    }



    public  void lambda$null$1$MenuActivity() {
        toggleStartButton(true);
    }

    
    public void toggleStartButton(boolean z) {
        if (z) {
            this.progressBar.setVisibility(View.INVISIBLE);
            this.startButton.setVisibility(View.VISIBLE);
            return;
        }
        this.progressBar.setVisibility(View.VISIBLE);
        this.startButton.setVisibility(View.INVISIBLE);
    }
}
