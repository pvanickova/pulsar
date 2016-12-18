package cz.kcck.android;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/*
 * This activity displays a tutorial for the app using page fragments.
 */
public class HelpActivity extends FragmentActivity {
	/**
	 * The number of pages to show in the tutorial.
	 */
	private static final int NUM_PAGES = 3;
	private List<ImageView> dots;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally
	 * to access previous and next pages.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);

		Button buttonCloseHelp = (Button) findViewById(R.id.buttonCloseHelp);
		buttonCloseHelp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		addDots();

	}

	@Override
	public void onBackPressed() {
		if (mPager.getCurrentItem() == 0) {
			// If the user is currently looking at the first step, allow the
			// system to handle the Back button. This calls finish() on this
			// activity and pops the
			// back stack.
			super.onBackPressed();
		} else {
			// Otherwise, select the previous step.
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
		}
	}

	private void addDots() {
		dots = new ArrayList<ImageView>();
		LinearLayout dotsLayout = (LinearLayout) findViewById(R.id.dots);

		for (int i = 0; i < NUM_PAGES; i++) {
			ImageView dot = new ImageView(this);
			dot.setImageDrawable(ContextCompat.getDrawable(this,
					R.drawable.page_indicator_unselected));

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			dotsLayout.addView(dot, params);

			dots.add(dot);
		}

		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				selectDot(position);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

		});

		selectDot(0);
	}

	private void selectDot(int idx) {
		for (int i = 0; i < NUM_PAGES; i++) {
			int drawableId = (i == idx) ? (R.drawable.page_indicator_selected)
					: (R.drawable.page_indicator_unselected);
			Drawable drawable = ContextCompat.getDrawable(this, drawableId);
			dots.get(i).setImageDrawable(drawable);
		}
	}

	/**
	 * A simple pager adapter that represents 3 ScreenSlidePageFragment objects,
	 * in sequence.
	 */
	private class TutorialPagerAdapter extends FragmentStatePagerAdapter {

		public TutorialPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			TutorialPageFragment f = new TutorialPageFragment(position);
			return f;
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}

	}
}
