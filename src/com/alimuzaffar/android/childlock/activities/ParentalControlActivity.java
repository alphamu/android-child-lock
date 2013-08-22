package com.alimuzaffar.android.childlock.activities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.alimuzaffar.android.childlock.Constants;
import com.alimuzaffar.android.childlock.R;
import com.alimuzaffar.android.childlock.fragments.ListOfApplicationsFragment;
import com.alimuzaffar.android.childlock.fragments.ListOfLockedApplicationsFragment;
import com.alimuzaffar.android.childlock.fragments.SetPinDialogFragment;
import com.alimuzaffar.android.childlock.utils.AppSettings;
import com.alimuzaffar.android.childlock.utils.AppSettings.Key;
import com.alimuzaffar.android.childlock.utils.Utils;

public class ParentalControlActivity extends FragmentActivity implements View.OnClickListener {
	private static final String TAG = ParentalControlActivity.class.getSimpleName();

	private boolean mCheckPin = false;

	private final int REQCODE_CHECK_PIN = 1011;
	private final int REQCODE_SET_PIN = 1012;

	ListOfApplicationsFragment mListOfAllAppsFragment;
	ListOfLockedApplicationsFragment mListOfLockedAppsFragment;

	private List<ResolveInfo> mInstalledApps;
	private List<ResolveInfo> mLockedApps;

	SetPinDialogFragment mSetPinDialog;

	Button mAll;
	Button mLocked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parental_control);

		boolean isPinSet = AppSettings.getInstance(this).getBoolean(Key.IS_PIN_SET);
		if (savedInstanceState == null && isPinSet) {
			Intent intent = new Intent(this, LockScreenActivity.class);
			intent.putExtra(Constants.EXTRA_PACKAGE_NAME, "com.alimuzaffar.android.childlock");
			intent.putExtra(Constants.EXTRA_COMPONENT_NAME, new ComponentName(this, ParentalControlActivity.class));
			startActivityForResult(intent, REQCODE_CHECK_PIN);
			mCheckPin = false;
			
		} 
		
		mAll = (Button) findViewById(R.id.all_apps);
		mLocked = (Button) findViewById(R.id.blocked_apps);

		mAll.setOnClickListener(this);
		mLocked.setOnClickListener(this);

		//set the all button as the default.
		mAll.setBackgroundResource(R.drawable.btn_parent_lock_tab_selected);
		
		initOrUpdateListOfApps();
		
		mListOfAllAppsFragment = ListOfApplicationsFragment.getInstance(mInstalledApps);
		mListOfLockedAppsFragment = ListOfLockedApplicationsFragment.getInstance(mLockedApps);

		if (findViewById(R.id.fragment_container) != null) {
			if (savedInstanceState != null && savedInstanceState.getBoolean("locked")) {
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mListOfLockedAppsFragment).commit();
				mAll.setBackgroundResource(R.drawable.btn_parent_lock_tab_unselected);
				mLocked.setBackgroundResource(R.drawable.btn_parent_lock_tab_selected);
			} else {
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mListOfAllAppsFragment).commit();
			}
		}

		updateLockedAppsCount();
	}
	
	@Override
	protected void onStart() {
		Log.i(TAG, "onStart");
		super.onStart();
		
		boolean isPinSet = AppSettings.getInstance(this).getBoolean(Key.IS_PIN_SET);
		if(!isPinSet) {
			mSetPinDialog = SetPinDialogFragment.newInstance();
			mSetPinDialog.setOnClickNo(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					ParentalControlActivity.this.finish();
					
				}
			});
			mSetPinDialog.setOnClickYes(new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent(ParentalControlActivity.this, LockScreenActivity.class);
					intent.putExtra(Constants.EXTRA_PACKAGE_NAME, "com.alimuzaffar.android.childlock");
					intent.putExtra(Constants.EXTRA_COMPONENT_NAME, new ComponentName(ParentalControlActivity.this, ParentalControlActivity.class));
					intent.putExtra(Constants.EXTRA_SET_PIN, true);
					startActivityForResult(intent, REQCODE_SET_PIN);
				}
			});
			mSetPinDialog.setCancelable(false);
			mSetPinDialog.show(getSupportFragmentManager(), TAG);
			
		}
	}
	
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i(TAG, "onSaveInstanceState");
		if(mSetPinDialog != null) {
			mSetPinDialog.dismiss();
		}

		super.onSaveInstanceState(outState);
		
		outState.putBoolean("locked", mListOfLockedAppsFragment.isVisible());
		outState.putBoolean("checkpin", mCheckPin);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.parental_lock_menu, menu);
		return true;
	}


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.menu_change_pin:
				intent = new Intent(this, LockScreenActivity.class);
				intent.putExtra(Constants.EXTRA_SET_PIN, true);
				intent.putExtra(Constants.EXTRA_FORCE_CLOSE, false);
				startActivityForResult(intent, REQCODE_SET_PIN);
				break;

		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onPause() {
		Log.i(TAG, "onPause");
		super.onPause();
		JSONObject json = new JSONObject();
		JSONArray locked = new JSONArray();
		
		PackageManager pm = getPackageManager();
		
		for(ResolveInfo ri : mLockedApps) {
			ApplicationInfo applicationInfo = ri.activityInfo.applicationInfo;
			locked.put(applicationInfo.packageName+":"+ri.loadLabel(pm));
		}
		
		try {
			json.put("locked", locked);
			FileOutputStream ostream = openFileOutput(Constants.LOCKED_APP_FILE, Context.MODE_PRIVATE);
			ostream.write(json.toString().getBytes());
			ostream.flush();
			ostream.close();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
	}
	
	@Override
	protected void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		mCheckPin = true;
		super.onBackPressed();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQCODE_CHECK_PIN) {
			if (data != null && data.getStringExtra("pin") != null) {
				mCheckPin = false;
			} else {
				mCheckPin = true;
			}
		} else if (requestCode == REQCODE_SET_PIN) {
			if (data != null && data.getStringExtra(Constants.EXTRA_PIN) != null) {
				AppSettings.getInstance(this).set(Key.IS_PIN_SET, true);
				AppSettings.getInstance(this).set(Key.PIN, data.getStringExtra(Constants.EXTRA_PIN));
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == mLocked.getId()) {
			// Create fragment and give it an argument specifying the
			// article it should show
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			mLocked.setBackgroundResource(R.drawable.btn_parent_lock_tab_selected);
			mAll.setBackgroundResource(R.drawable.btn_parent_lock_tab_unselected);

			transaction.replace(R.id.fragment_container, mListOfLockedAppsFragment);
			// transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commitAllowingStateLoss();
		} else if (v.getId() == mAll.getId()) {
			// Create fragment and give it an argument specifying the
			// article it should show
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			mLocked.setBackgroundResource(R.drawable.btn_parent_lock_tab_unselected);
			mAll.setBackgroundResource(R.drawable.btn_parent_lock_tab_selected);

			transaction.replace(R.id.fragment_container, mListOfAllAppsFragment);
			// transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commitAllowingStateLoss();
		}

	}

	public void updateLockedAppsCount() {
		mLocked.setText(String.format("Blocked (%d)", mLockedApps.size()));
	}
	
	private void initOrUpdateListOfApps() {
		String[] files = fileList();
		boolean found = false;
		for (String f : files) {
			if (f.equals(Constants.LOCKED_APP_FILE)) {
				found = true;
				break;
			}
		}
			
		initListOfApps();

		if (found) {
			updateLockedApps();
		}
	}
	
	private void updateLockedApps() {
		try {
			FileInputStream istream = openFileInput(Constants.LOCKED_APP_FILE);
			String jsonStr = Utils.getStringFromInputStream(istream);
			istream.close();
			if (jsonStr != null && jsonStr.length() > 0) {
				JSONObject	json = new JSONObject(jsonStr);
				JSONArray locked = json.optJSONArray("locked");
				if(locked != null) {
					for(int i=0; i<locked.length(); i++) {
						String packageName = locked.getString(i);
						int split = packageName.indexOf(':');
						String pkg = packageName.substring(0, split);
						String lbl = packageName.substring(split+1);
						int index = getIndexOfApp(pkg, lbl);
						if(index > -1) {
							ResolveInfo ri = mInstalledApps.remove(index);
							mLockedApps.add(ri);
						}
					}
				}
			}
			
		} catch(Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void initListOfApps() {
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final PackageManager packageManager = getPackageManager();
		mLockedApps = new ArrayList<ResolveInfo>();
		mInstalledApps = packageManager.queryIntentActivities(mainIntent, 0);
		Comparator<ResolveInfo> comparator = new Comparator<ResolveInfo>() {

			@Override
			public int compare(ResolveInfo lhs, ResolveInfo rhs) {
				CharSequence l = lhs.loadLabel(packageManager);
				CharSequence r = rhs.loadLabel(packageManager);

				return l.toString().compareTo(r.toString());
			}
		};
		
		Collections.sort(mInstalledApps, comparator);
	}
	
	private int getIndexOfApp(String packageName, String lbl) {
		for(int i=0; i<mInstalledApps.size(); i++) {
			ResolveInfo ri = mInstalledApps.get(i);
			ApplicationInfo applicationInfo = ri.activityInfo.applicationInfo;
			if (applicationInfo.packageName.equals(packageName)) {
				String label = ri.loadLabel(getPackageManager()).toString();
				if(label.equals(lbl))
					return i;
			}
		}
		return -1;
	}

	public void lockApp(int position) {
		ResolveInfo ri = mInstalledApps.remove(position);
		mLockedApps.add(ri);
		updateLockedAppsCount();
	}
	
	public void unlockApp(int position) {
		ResolveInfo ri = mLockedApps.remove(position);
		mInstalledApps.add(ri);
		updateLockedAppsCount();
	}

}
