package com.alimuzaffar.android.childlock.fragments;

import java.util.List;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.alimuzaffar.android.childlock.Constants;
import com.alimuzaffar.android.childlock.activities.ParentalControlActivity;
import com.alimuzaffar.android.childlock.adapters.EfficientAdapter;
import com.alimuzaffar.android.childlock.utils.AppPrefs;
import com.alimuzaffar.android.childlock.utils.AppSettings;
import com.alimuzaffar.android.childlock.utils.AppSettings.Key;

public class ListOfLockedApplicationsFragment extends ListFragment implements ListView.OnScrollListener {

	public final String TAG = ListOfLockedApplicationsFragment.class.getSimpleName();

	EfficientAdapter mAdapter;
	
	private static ListOfLockedApplicationsFragment instance;
	
	private List<ResolveInfo> mLockedApps;
	
    public static ListOfLockedApplicationsFragment getInstance(List<ResolveInfo> lockedApps)
    {
        if (instance == null) {
            instance = new ListOfLockedApplicationsFragment();
            
        }
        instance.setList(lockedApps);
        return instance;
    }
    
    void setList(List<ResolveInfo> lockedApps) {
    	mLockedApps = lockedApps;
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ListView listView = getListView();
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		listView.setOnScrollListener(this);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mAdapter = new EfficientAdapter(getActivity(), mLockedApps);
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onListItemClick(ListView l, final View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
		Animation anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
		anim.setDuration(500);

		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
					v.setHasTransientState(true);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				((ParentalControlActivity)getActivity()).unlockApp(position);
				mAdapter.notifyDataSetChanged();
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
					v.setHasTransientState(false);
			}
		});

		v.startAnimation(anim);
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				mAdapter.setBusy(false, view);
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				mAdapter.setBusy(true, view);
				break;
			case OnScrollListener.SCROLL_STATE_FLING:
				mAdapter.setBusy(true, view);
				break;
		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(data != null && data.getStringExtra(Constants.EXTRA_PIN) != null) {
	    	String pin = data.getStringExtra(Constants.EXTRA_PIN);
	    	AppSettings settings = AppSettings.getInstance(getActivity());
	    	settings.set(Key.IS_PIN_SET, true);
	    	settings.set(Key.PIN, pin);
	    	AppPrefs.getInstance(getActivity()).set(com.alimuzaffar.android.childlock.utils.AppPrefs.Key.ENABLE_APPLICATION_LOCKING, true);
	    }
	}
	
}
