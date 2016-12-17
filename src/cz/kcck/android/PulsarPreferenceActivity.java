package cz.kcck.android;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class PulsarPreferenceActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
	}
	
	
	public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
    		addPreferencesFromResource(R.xml.preferences);
    		setValidationToPreference("editText_pulseFrequency"); 
    		setValidationToPreference("editText_pulseCounterMax");

        }
        
        private void setValidationToPreference(String preferenceName) {
    		findPreference(preferenceName).setOnPreferenceChangeListener(
    				new OnPreferenceChangeListener() {

    					@Override
    					public boolean onPreferenceChange(Preference pref,
    							Object newValue) {
    						return checkNotEmpty(pref, (String) newValue);
    					}
    				});
    	}

    	private boolean checkNotEmpty(Preference pref, String value) {

    		if (value == null || value.equals("")) {
    			Toast.makeText(this.getActivity(),
    					"Preference " + pref.getTitle() + " cannot be empty.",
    					Toast.LENGTH_SHORT).show();
    			return false;
    		}
    		
    		if (Integer.parseInt(value)<=0) {
    			Toast.makeText(this.getActivity(),
    					"Preference " + pref.getTitle() + " cannot be less than or equal to zero.",
    					Toast.LENGTH_SHORT).show();
    			return false;
    		}
    				
    		if(pref.getKey().equalsIgnoreCase("editText_pulseFrequency"))
    		{
    			if(!checkMax(value,300))
    			{
    				Toast.makeText(this.getActivity(),
    						"Preference " + pref.getTitle() + " cannot be greater than 300.",
    						Toast.LENGTH_SHORT).show();
    				return false;
    			}
    		}
    		if(pref.getKey().equalsIgnoreCase("editText_pulseCounterMax"))
    		{
    			if(!checkMax(value,1000))
    			{
    				Toast.makeText(this.getActivity(),
    						"Preference " + pref.getTitle() + " cannot be greater than 1000.",
    						Toast.LENGTH_SHORT).show();
    				return false;
    			}		
    		}
    		return true;
    	}

    	private boolean checkMax(String value, int max) {
    		return Integer.parseInt(value)<=max;		
    	}

    }

	

}
