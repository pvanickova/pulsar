package cz.kcck.android;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * This activity displays preferences that user can configure i.e. ambulance phone number, pulse frequency and max counter limit.
 */
public class PulsarPreferenceActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
	}

	public static class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);
			setValidationToPreference("editText_pulseFrequency");
			setValidationToPreference("editText_pauseForRescueBreaths");

		}

		private void setValidationToPreference(String preferenceName) {
			findPreference(preferenceName).setOnPreferenceChangeListener(
					new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference pref,
								Object newValue) {
							return checkPreferences(pref, (String) newValue);
						}
					});
		}

		private boolean checkPreferences(Preference pref, String value) {

			if (checkEmpty(pref, value)) return false;

			if (pref.getKey().equalsIgnoreCase("editText_pulseFrequency")) {
				if (!checkPositiveOrZeroInteger(pref, value)) return false;
				if (!checkMaxInteger(pref, value,300)) return false;
			}
			if (pref.getKey().equalsIgnoreCase("editText_pauseForRescueBreaths")) {
				if (!checkPositiveOrZeroFloat(pref, value)) return false;
				if (!checkMaxFloat(pref, value,60)) return false;
			}
			return true;
		}

		private boolean checkEmpty(Preference pref, String value) {
			if (value == null || value.equals("")) {
				Toast.makeText(this.getActivity(),
						"Preference " + pref.getTitle() + " cannot be empty.",
						Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}

		private boolean checkPositiveOrZeroFloat(Preference pref, String value) {
			if (Float.parseFloat(value) <= 0) {
				Toast.makeText(
						this.getActivity(),
						"Preference " + pref.getTitle()
								+ " cannot be less than or equal to zero.",
						Toast.LENGTH_SHORT).show();
				return false;
			}
			return true;
		}

		private boolean checkPositiveOrZeroInteger(Preference pref, String value) {
			if (Integer.parseInt(value) <= 0) {
				Toast.makeText(
						this.getActivity(),
						"Preference " + pref.getTitle()
								+ " cannot be less than or equal to zero.",
						Toast.LENGTH_SHORT).show();
				return false;
			}
			return true;
		}

		private boolean checkMaxInteger(Preference pref, String value, int max) {

			if ( Integer.parseInt(value) > max) {
				Toast.makeText(
						this.getActivity(),
						"Preference " + pref.getTitle()
								+ " cannot be greater than "+value+".",
						Toast.LENGTH_SHORT).show();
				return false;
			}
			return true;
		}

		private boolean checkMaxFloat(Preference pref, String value, float max) {

			if ( Float.parseFloat(value) > max) {
				Toast.makeText(
						this.getActivity(),
						"Preference " + pref.getTitle()
								+ " cannot be greater than "+value+".",
						Toast.LENGTH_SHORT).show();
				return false;
			}
			return true;
		}

	}

}
