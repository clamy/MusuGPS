package musuGPS.main;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.obj.MemObj;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class SharedGPSLocationListener implements LocationListener{
	private DbFeed feed;
	private static final String LAT = "lat";
	private static final String LON = "lon";
	private LocationManager locMan;
	private SharedPreferences settings;
	private int id;
	private int conf;
	
	public SharedGPSLocationListener(DbFeed feed, LocationManager locMan, SharedPreferences settings, int id,int conf){
		this.feed = feed;
		Location lastKnownLocation = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(lastKnownLocation == null){
			lastKnownLocation = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		postStart(feed);
		if(lastKnownLocation != null){
			postLocation((int)(lastKnownLocation.getLatitude()*1E6),(int)(lastKnownLocation.getLongitude()*1E6),feed);
		}
		this.locMan = locMan;
		this.settings = settings;
		this.id = id;
	}
	
	public void onLocationChanged(Location location) {
		Boolean post = settings.getBoolean("sharing"+id, false);
		int locMode = (int) settings.getLong("locationMode"+id, 0);
		if(post && locMode == 0 && location != null){
			postLocation((int)(location.getLatitude()*1E6),(int)(location.getLongitude()*1E6),feed);
			int currentConf = (int) settings.getLong("sharedMode"+id, 0);
			if(currentConf != conf){
				conf = currentConf;
				if(conf == 0){
					locMan.removeUpdates(this);
					locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 120000, 10, this);
				}
				else{
					locMan.removeUpdates(this);
					locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
				}
			}
		}
		else{
			locMan.removeUpdates(this);
		}
	}
	
	public static void postLocation(int lat, int lon, DbFeed feed){
		
		      String loc = feed.getLocalUser().getId()+":"+lat+":"+lon ;
		      JSONObject meta = new JSONObject();
		      try {
				meta.put(Obj.TYPE_TEXT, loc);
				feed.insert(new  MemObj(SharedGPSActivity.TYPE_MUSUGPS_POSITION, meta ));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	public static  void postStart(DbFeed feed){
		String id = feed.getLocalUser().getId();
		String msg = "<body>I started sharing my location</body>";
		JSONObject meta = new JSONObject();
		try {
			meta.put(Obj.TYPE_TEXT, id);
			meta.put(Obj.FIELD_HTML, msg);

			feed.insert(new  MemObj(SharedGPSActivity.TYPE_MUSUGPS_STATUS, meta ));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void postStop(DbFeed feed){
		String id = feed.getLocalUser().getId();
		String msg = "<body>I stopped sharing my location</body>";
		JSONObject meta = new JSONObject();
	      try {
	    	meta.put(Obj.TYPE_TEXT, id);
			meta.put(Obj.FIELD_HTML, msg);
			feed.insert(new  MemObj(SharedGPSActivity.TYPE_MUSUGPS_STATUS, meta ));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onProviderDisabled(String provider) {
		postStop(feed);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("sharing"+id, false);
		editor.commit();
		locMan.removeUpdates(this);
		
	}

	public void onProviderEnabled(String provider) {	
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	protected void finalize(){
		Boolean sharing = settings.getBoolean("sharing"+id, false);
		int locationMode = (int) settings.getLong("locationMode"+id, 0);
		if(sharing && locationMode == 0){
			postStop(feed);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("sharing"+id, false);
			editor.commit();			
		}
	}
	

}
