package cz.kcck.android;
import cz.kcck.android.R;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TutorialPageFragment extends Fragment {
	
	private int position=0; 
	
	public TutorialPageFragment() {
	}
	
	public TutorialPageFragment(int position) {		
		this.position = position;
		 Bundle args = new Bundle();
        args.putInt("position", position);
        this.setArguments(args);
	}
	
	 @Override
	    public void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        outState.putInt("position", position);
	    }

	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
		 
		 	if (savedInstanceState != null) {
	            // Restore last state for checked position.
	            position = savedInstanceState.getInt("position", 0);
	        }
		 
	        ViewGroup rootView = (ViewGroup) inflater.inflate(
	                R.layout.fragment_tutorial_page, container, false);
	        
	        ImageView mHelpImage = (ImageView) rootView.findViewById(R.id.imageViewHelp);	        
	        int imageResourceId = getResources().getIdentifier("help_page_"+position, "drawable", getActivity().getPackageName());
	        mHelpImage.setImageResource(imageResourceId);
	        
	        TextView mHelpText = (TextView)rootView.findViewById(R.id.textHelp);
	        int textResourceId = getResources().getIdentifier("help_page_"+position, "string", getActivity().getPackageName());
	        mHelpText.setText(textResourceId);
	        
	        return rootView;
	    }

}
