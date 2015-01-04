package com.aaha.alsalah;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.aaha.alsalah.settings.Settings;

public class Splash extends Activity implements AnimationListener {
	TextView txtTitle, txtSubTitle;
	Animation animFadeIn, animFadeIn2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		boolean showsplash = Settings.getBoolean(Settings.PREF_SPLASH,
				getApplicationContext(), true);
		if (!showsplash) {
			gotoHomePage();
		}
		animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.fade_in);
		animFadeIn.setAnimationListener(this);

		txtSubTitle = (TextView) findViewById(R.id.txtSubTitle);
		txtSubTitle.startAnimation(animFadeIn);
	}

	private void gotoHomePage() {
		Intent i = new Intent(this, Home.class);
		startActivity(i);
		super.finish();
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == animFadeIn) {
			gotoHomePage();
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
	}
}
