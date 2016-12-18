package cz.kcck.android;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/*
 * The main activity for pulsar cpr.
 */
public class PulsarActivity extends AppCompatActivity {
	private static final String LOG_TAG = "PulsarActivity";
	
	private short pulseCount = 0;
	private short maxPulseCount = 30;
	private short pulseFrequency = 100;
	private String ambulancePhoneNumber = "112";
	private boolean licenseAccepted = false;
	private long startTime;
	
	private SoundPool soundPool;
	private int soundID;
	boolean soundLoaded = false;
	boolean paused = true;
	
	private Resources res;
	private TextView textViewTime = null;
	private TextView textViewTimeStart = null;
	
	private Handler pulseHandler = new Handler();		
	private Runnable updatePulseTask = new Runnable() {
		public void run() {
			long millisSinceStart = (SystemClock.elapsedRealtime() - startTime);
			long cycleLength = (60 * 1000 / pulseFrequency);
			long drift = millisSinceStart % cycleLength;
			// Log.d(LOG_TAG,"millis " + millisSinceStart + " drift: "+drift);
			pulseHandler.postDelayed(updatePulseTask, cycleLength - drift);
			doPulse();
		}

		private void doPulse() {
			if (!licenseAccepted)
				return;
			playSound();
			pulseCount++;
			if (pulseCount > maxPulseCount) {
				pulseCount = 1;
			}
			((TextView) findViewById(R.id.textViewCounter)).setText(""
					+ pulseCount);
			findViewById(R.id.imageViewPulse).startAnimation(pulseAnimation);

		}
	};
	
	private Handler timeHandler = new Handler();
	private Runnable updateTimerTask = new Runnable() {
		public void run() {
			long millis = SystemClock.elapsedRealtime() - startTime;
			doTimer(millis);
			timeHandler.postDelayed(updateTimerTask, 100);
		}

		private void doTimer(long millis) {
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			if (seconds < 10) {
				textViewTime.setText("" + minutes + ":0" + seconds);
			} else {
				textViewTime.setText("" + minutes + ":" + seconds);
			}
		}

	};
	
	private Animation pulseAnimation;

	public static GoogleAnalytics analytics;
	public static Tracker tracker;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startTime = SystemClock.elapsedRealtime();
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		res = getResources();
		initPreferences();
		reloadState(savedInstanceState);
		setContentView(R.layout.main);
		licenseAccepted();
		initButtons();
		initPulseAnimation((ImageView) findViewById(R.id.imageViewPulse));
		initCounters();
		initSound();
		paused = false;
		initAnalytics();
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		pulseHandler.removeCallbacksAndMessages(null);
		timeHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onPause() {
		super.onPause();
		soundPool.autoPause();
		paused = true;
	}

	@Override
	public void onResume() {
		super.onResume();
		soundPool.autoResume();
		paused = false;
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			reloadPreferences();
			Log.d(LOG_TAG, "Reloaded preferences: pulse frequency="
					+ pulseFrequency + " max pulse count=" + maxPulseCount);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * This method is called once the menu item is selected
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.xml.preferences:
			tracker.send(new HitBuilders.EventBuilder().setCategory("Buttons")
					.setAction("click").setLabel("Preferences").build());
			// Launch Preference activity
			Intent i = new Intent(PulsarActivity.this,
					PulsarPreferenceActivity.class);
			startActivity(i);
			break;
		case R.id.action_restart:
			tracker.send(new HitBuilders.EventBuilder().setCategory("Buttons")
					.setAction("click").setLabel("Restart").build());

			pulseCount = 0;
			startTime = SystemClock.elapsedRealtime();
			initCounters();
			break;
		case R.id.action_info:
			tracker.send(new HitBuilders.EventBuilder().setCategory("Buttons")
					.setAction("click").setLabel("Info").build());

			Intent ih = new Intent(PulsarActivity.this, HelpActivity.class);
			startActivity(ih);
			break;
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("startTime", startTime);
		outState.putShort("pulseCount", pulseCount);
	}

	/**
	 * Manages the appearance of the terms & conditions.
	 */
	private void licenseAccepted() {

		if (licenseAccepted) {
			return;
		}

		InputStream in_s = res.openRawResource(R.raw.disclaimer);
		byte[] b = null;
		try {
			b = new byte[in_s.available()];
			in_s.read(b);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.licenseTermsAndConditions)
					.setMessage(new String(b))
					.setCancelable(false)
					.setPositiveButton(res.getString(R.string.licenseAgree),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									tracker.send(new HitBuilders.EventBuilder()
											.setCategory("License")
											.setAction("click")
											.setLabel("Approved").build());

									SharedPreferences sharedPref = PreferenceManager
											.getDefaultSharedPreferences(PulsarActivity.this);
									SharedPreferences.Editor editor = sharedPref
											.edit();
									editor.putBoolean("licenseAccepted", true);
									editor.commit();
									Intent ih = new Intent(PulsarActivity.this,
											HelpActivity.class);
									startActivity(ih);

								}
							})
					.setNegativeButton(res.getString(R.string.licenseDisagree),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									tracker.send(new HitBuilders.EventBuilder()
											.setCategory("License")
											.setAction("click")
											.setLabel("Rejected").build());

									System.exit(0);
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Could not load the legal disclaimer.");
		}

	}

	/**
	 * Sets up button for ambulance call.
	 */
	private void initButtons() {
		ImageButton callAmbulanceButton = (ImageButton) findViewById(R.id.ImageButtonCallAmbulance);
		callAmbulanceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				callAmbulance();
			}
		});
	}

	/**
	 * Starts the ambulance call.
	 */
	private void callAmbulance() {

		final EditText inputAmbulancePhoneNumber = new EditText(this);
		inputAmbulancePhoneNumber.setText(ambulancePhoneNumber);
		inputAmbulancePhoneNumber.setInputType(InputType.TYPE_CLASS_PHONE);
		
		AlertDialog alertDialog = new AlertDialog.Builder(this).create(); 
		alertDialog.setTitle(R.string.callAmbulanceQuestion);	
		alertDialog.setView(inputAmbulancePhoneNumber);
		
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
				res.getString(R.string.callAmbulanceYes),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						tracker.send(new HitBuilders.EventBuilder()
								.setCategory("Buttons").setAction("click")
								.setLabel("Ambulance called").build());

						String ambulancePhoneNumber = inputAmbulancePhoneNumber
								.getText().toString().trim();
						try {
							AudioManager audioManager = (AudioManager) PulsarActivity.this
									.getSystemService(Context.AUDIO_SERVICE);
							audioManager.setMode(AudioManager.MODE_IN_CALL);
							audioManager.setSpeakerphoneOn(true);
							Intent callIntent = new Intent(Intent.ACTION_CALL);
							callIntent.setData(Uri.parse("tel:"
									+ ambulancePhoneNumber));
							startActivity(callIntent);
						} catch (ActivityNotFoundException e) {
							Log.e(LOG_TAG, "Call to " + ambulancePhoneNumber
									+ " failed", e);
							Toast.makeText(
									getApplicationContext(),
									res.getString(R.string.callAmbulanceFailed,
											ambulancePhoneNumber),
									Toast.LENGTH_SHORT).show();
						}
					}
				});

		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				res.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						tracker.send(new HitBuilders.EventBuilder()
								.setCategory("Buttons").setAction("click")
								.setLabel("Ambulance call cancelled").build());

						dialog.cancel();
					}
				});

		alertDialog.show();
	}
	
	private void initPreferences() {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		reloadPreferences();
	}

	/**
	 * Reloads persisted user preferences for counters and ambulance phone number.
	 */
	private void reloadPreferences() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		maxPulseCount = Short.parseShort(sharedPref.getString(
				"editText_pulseCounterMax", null));
		pulseFrequency = Short.parseShort(sharedPref.getString(
				"editText_pulseFrequency", null));
		ambulancePhoneNumber = sharedPref.getString(
				"editText_ambulancePhoneNumber", null);
		licenseAccepted = sharedPref.getBoolean("licenseAccepted", false);
	}

	/**
	 * Initializes time and pulse counters.
	 */
	private void initCounters() {
		Log.d(LOG_TAG, "init counters");

		timeHandler.removeCallbacks(updateTimerTask);
		timeHandler.post(updateTimerTask);
		
		pulseHandler.removeCallbacks(updatePulseTask);
		pulseHandler.post(updatePulseTask);
		
		textViewTime = (TextView) findViewById(R.id.textViewTime);
		textViewTimeStart = (TextView) findViewById(R.id.textViewTimeStart);

		Date startDate = new Date(System.currentTimeMillis()
				- SystemClock.elapsedRealtime() + startTime);
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		textViewTimeStart.setText("" + format.format(startDate));
	}

	private void initPulseAnimation(ImageView myImageView) {
		pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
		AnimationListener animationListener = new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				findViewById(R.id.imageViewPulse).setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
				// do nothing
			}

			// at the end of the animation, start new activity
			public void onAnimationEnd(Animation animation) {
				findViewById(R.id.imageViewPulse).setVisibility(View.INVISIBLE);
			}
		};
		pulseAnimation.setAnimationListener(animationListener);
	}

	private void initSound() {
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// Load the sound
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool
				.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
					@Override
					public void onLoadComplete(SoundPool soundPool,
							int sampleId, int status) {
						soundLoaded = true;
					}
				});
		soundID = soundPool.load(this, R.raw.beep, 1);
	}
	
	/**
	 * Initializes google analytics communication.
	 */
	private void initAnalytics() {
		analytics = GoogleAnalytics.getInstance(this);
		analytics.setLocalDispatchPeriod(1800);

		tracker = analytics.newTracker("UA-65181986-1"); 
		tracker.enableExceptionReporting(true);
		tracker.enableAdvertisingIdCollection(false);
		tracker.enableAutoActivityTracking(true);
		tracker.setScreenName("main screen");
	}


	/**
	 * Reloads persisted start time and pulse count.
	 * 
	 * @param savedInstanceState
	 */
	private void reloadState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			startTime = savedInstanceState.getLong("startTime");
			pulseCount = savedInstanceState.getShort("pulseCount");
		}
	}

	/**
	 * Plays the beep sound when a chest compression should appear.
	 */
	private void playSound() {
		if (paused)
			return;
		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		float actualVolume = (float) audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = actualVolume / maxVolume;
		// Is the sound loaded already?
		if (soundLoaded) {
			soundPool.play(soundID, volume, volume, 1, 0, 1f);
		}
	}

}