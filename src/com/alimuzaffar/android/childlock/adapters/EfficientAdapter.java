package com.alimuzaffar.android.childlock.adapters;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.alimuzaffar.android.childlock.R;

public class EfficientAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private List<ResolveInfo> mData;
    
    private PackageManager mPackageManager;
    private boolean mBusy = false;
    
    public EfficientAdapter(Context context, List<ResolveInfo> installedAppsList) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        mPackageManager = context.getPackageManager();
        mData = installedAppsList;
        mContext = context;
    }

    /**
     * The number of items in the list is determined by the number of speeches
     * in our array.
     *
     * @see android.widget.ListAdapter#getCount()
     */
    public int getCount() {
    	if(mData == null)
    		return 0;
        return mData.size();
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficent to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     *
     * @see android.widget.ListAdapter#getItem(int)
     */
    public Object getItem(int position) {
        return position;
    }

    /**
     * Use the array index as a unique id.
     *
     * @see android.widget.ListAdapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_icon_text, null);

            holder = new ViewHolder();
            holder.text = (CheckedTextView) convertView.findViewById(R.id.text1);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        ResolveInfo ri = mData.get(position);
        holder.text.setText(ri.loadLabel(mPackageManager));
        
        Resources r = mContext.getResources();
    	float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());
    	
    	Drawable d = ri.loadIcon(mPackageManager);
    	
    	//only resize of the icon is too big since it's an expensive operation
    	if(d.getIntrinsicHeight() > px && !mBusy) {
    		Log.d(getClass().getSimpleName(), String.format("RESIZING icon => %d   us => %s", d.getIntrinsicHeight(), px));
    		d = resize(r, d, (int) px);
    		holder.text.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
    	} else if (d.getIntrinsicHeight() <= px) {
    		holder.text.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
    	} else {
    		holder.text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_launcher_default, 0, 0, 0);
    	}
    	
        return convertView;
    }

    private Drawable resize(Resources r, Drawable image, int newSize) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, newSize, newSize, false);
        BitmapDrawable drawableBmp = new BitmapDrawable(r, bitmapResized);
        return drawableBmp;
    }
    
    public void setBusy(boolean busy, AbsListView view) {
    	mBusy = busy;
    	if(!mBusy) {
            Resources r = mContext.getResources();
        	float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());
    		
    		int first = view.getFirstVisiblePosition();
            int count = view.getChildCount();
            for (int i=0; i<count; i++) {
                CheckedTextView t = (CheckedTextView)view.getChildAt(i);
                if (t.getTag() != null) {
                	ResolveInfo ri = mData.get(first+i);
                	Drawable d = ri.loadIcon(mPackageManager);
                	
                	//only replace the icons that were too large
                	if(d.getIntrinsicHeight() > px) {
                		Log.d(getClass().getSimpleName(), String.format("RESIZING icon => %d   us => %s", d.getIntrinsicHeight(), px));
                		d = resize(r, d, (int) px);
                		t.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                	} 
                    //t.setTag(null);
                }
            }
    	}
    }
    
    class ViewHolder {
        CheckedTextView text;
    }
}