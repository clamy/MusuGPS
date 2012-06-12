package musuGPS.main;

import mobisocial.socialkit.musubi.DbFeed;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SharingListener implements OnClickListener {
	
	SharedPreferences settings;
	Boolean lastValue;
	int id;
	DbFeed feed;
	LocationManager locMan;
	MapTouchListener mtl;
	public SharingListener(SharedPreferences settings, Boolean start,int id, DbFeed feed, LocationManager locMan,MapTouchListener mtl){
		this.settings = settings;
		this.lastValue = start;
		this.id = id;
		this.feed = feed;
		this.locMan = locMan;
		this.mtl = mtl;
	}
	
	public void onClick(View v) {
		lastValue = ! lastValue;
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("sharing"+id, lastValue);
		editor.commit();
		if(lastValue){
			int locationMode = (int) settings.getLong("locationMode"+id, 0);
			if(locationMode == 0){
				int shareMode = (int) settings.getLong("sharedMode"+id, 0);
				SharedGPSLocationListener locationListener = new SharedGPSLocationListener(feed,locMan,settings,id,shareMode);
				if(shareMode == 0){
					locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120000, 10, locationListener);
				}
				else{
					locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
				}
			}
			else {
				SharedGPSLocationListener.postStart(feed);
				mtl.sendFalseLoc();
			}
		}
		else{
			SharedGPSLocationListener.postStop(feed);
		}
		
	}

	
}
