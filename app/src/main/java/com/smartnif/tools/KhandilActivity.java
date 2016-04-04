package com.smartnif.tools;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

import com.vipgroup.tools.R;

public class KhandilActivity extends Activity{

	private boolean isFlashLightOn = false;
	private Camera flashCamera;
	private Button flashButton;
	private WakeLock screenLock = null;


	AudioManager audioManager;
	private SoundPool soundPool;
	private int soundID[];
	boolean plays = false, loaded = false;
	float actVolume, maxVolume, volume;

	Animation animation;

	@Override
	protected void onStop() {
		super.onStop();

//		Log.i("info", "onStop()");

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

//		Log.i("info", "onDestroy()");
	}


	@Override
	protected void onPause() {
		super.onPause();

//		Log.i("info", "onPause()");

		if(screenLock != null){
			screenLock.release();
		}

		if (flashCamera!= null) {
			flashCamera.release();
		}

		isFlashLightOn = false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Log.i("info", "onCreate()");

	}


	@Override
	protected void onResume() {
		super.onResume();
//		Log.i("info", "onResume()");
		setContentView(R.layout.main);
		
		animation = new ScaleAnimation(
				0.8f, 1, 0.8f, 1, // From x, to x, from y, to y
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(800);
		animation.setFillAfter(true); 
		animation.setStartOffset(0);
		animation.setRepeatCount(1);
		animation.setRepeatMode(Animation.REVERSE);

		// AudioManager audio settings for adjusting the volume
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = actVolume / maxVolume;

		//Hardware buttons setting to adjust the media sound
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Load the sounds
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				loaded = true;
			}
		});

		soundID = new int[2];
		soundID[0] = soundPool.load(this, R.raw.b_on, 1);
		soundID[1] = soundPool.load(this, R.raw.b_off, 2);

		screenLock = ((PowerManager)getBaseContext().getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Screen");
		screenLock.acquire();

		flashButton = (Button) findViewById(R.id.buttonFlashlight);

		Context context = this;
		PackageManager pm = context.getPackageManager();

		// control, device support camera
		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Log.e("err", "Device has no camera!");
			return;
		}

		flashCamera= Camera.open();
		final Parameters p = flashCamera.getParameters();

		final Resources res;
		res = getResources();

		flashButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (isFlashLightOn) {

					Log.i("info", "torch is turn off!");

					LinearLayout bgElement = (LinearLayout) findViewById(R.id.flashScreen);
					bgElement.setBackgroundDrawable(getResources().getDrawable(R.drawable.main_background_gradient_off));
					//				bgElement.setBackgroundColor(Color.argb(22,22,22,25));  //#343437


					flashButton.setBackgroundDrawable(res.getDrawable(R.drawable.turnon));
					p.setFlashMode(Parameters.FLASH_MODE_OFF);
					flashCamera.setParameters(p);
					flashCamera.stopPreview();
					isFlashLightOn = false;


					soundPool.play(soundID[1], volume, volume, 1, 0, 1f);
					
					
					if (animation != null) {
						flashButton.startAnimation(animation);
					}

				} else {

					Log.i("info", "torch is turn on!");

					LinearLayout bgElement = (LinearLayout) findViewById(R.id.flashScreen);
					bgElement.setBackgroundDrawable(getResources().getDrawable(R.drawable.main_background_gradient_on));
					//				bgElement.setBackgroundColor(Color.argb(153,153,153,144));  //#343437

					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
					flashButton.setBackgroundDrawable(res.getDrawable(R.drawable.turnoff));
					flashCamera.setParameters(p);
					flashCamera.startPreview();
					isFlashLightOn = true;

					soundPool.play(soundID[0], volume, volume, 1, 0, 1f);
					
					if (animation != null) {
						flashButton.startAnimation(animation);
					}
				}

			}
		});

	}


}