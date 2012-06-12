package musuGPS.main;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.obj.MemObj;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MapTouchListener {	

	MapController mapController;	
	LocationManager locMan;
	Resources res;
	GeoPoint GPSloc;
	GeoPoint falseLoc;
	GeoPoint msgLoc;
	MusuMapOverlay overlay ;	
	Boolean hasGPS = false;
	Boolean hasFalse = false;
	Boolean hasMsg = false;	
	Boolean processEvent = false;
	Boolean isFalse = false;
	Context context;
	MapView mapView;
	SharedPreferences settings;
	DbFeed feed;
	SharedGPSActivity sga;
	int id;
	int mode = 0;
	
	public MapTouchListener(MapController mapCon,LocationManager locMan, Resources res, MapView mapView, SharedPreferences settings, DbFeed feed, SharedGPSActivity sga,Context context){		
		this.mapController = mapCon;
		this.locMan = locMan;
		this.res = res;
		this.mapView = mapView;
		this.settings = settings;
		this.feed = feed;
		this.sga = sga;
		this.context = context;
		
		overlay = new MusuMapOverlay(res, this);
		id = (int)feed.getLocalId();
		updateGPS();
		initializeFalseMsg();
		mapController.setZoom(16);
	}
	private void initializeFalseMsg(){
		if(settings.getBoolean("hasFalse"+id, false)){
			
			falseLoc = new GeoPoint((int)settings.getLong("latFalse"+id, 0),(int)settings.getLong("lonFalse"+id, 0));			
		}
		else {
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong("latFalse"+id, GPSloc.getLatitudeE6());
			editor.putLong("lonFalse"+id, GPSloc.getLongitudeE6());
			editor.putBoolean("hasFalse"+id, true);
			editor.commit();
			falseLoc = GPSloc;
		}
		if(settings.getBoolean("hasMsg"+id, false)){
			msgLoc = new GeoPoint((int)settings.getLong("latMsg"+id, 0),(int)settings.getLong("lonMsg"+id, 0));
		}
		else {
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong("latMsg"+id, GPSloc.getLatitudeE6());
			editor.putLong("lonMsg"+id, GPSloc.getLongitudeE6());
			editor.putBoolean("hasMsg"+id, true);
			editor.commit();
			msgLoc = GPSloc;
		}
		hasMsg = true;
		hasFalse = true;		
		redraw();
	}
	public void updateGPS(){
		Location lastKnownLocation = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(lastKnownLocation == null){
			lastKnownLocation = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if(lastKnownLocation != null){
			GPSloc = locToPoint(lastKnownLocation);
			if(hasGPS == false){
				hasGPS = true;				
			}
			redraw();
		}
	}
	public void sendFalseLoc(){
		SharedGPSLocationListener.postLocation(falseLoc.getLatitudeE6(), falseLoc.getLongitudeE6(), feed);
	}
	public void updateFalse(int lat, int lon){
		falseLoc = new GeoPoint(lat, lon);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("latFalse"+id, lat);
		editor.putLong("lonFalse"+id, lon);
		editor.putBoolean("hasFalse"+id, true);
		editor.commit();
		if(settings.getBoolean("sharing"+id, false) && settings.getLong("locationMode"+id, 0)==1){
			sendFalseLoc();
		}
		redraw();
	}
	public void update(int lat, int lon, int latmargin, int lonmargin){
		if(mode == 1){
			updateFalse(lat,lon);
		}
		else if(mode ==2){
			updateMsg(lat,lon);
		}
		else{
			LinkedList<String> toDisplay = getInfo(lat,lon,latmargin,lonmargin);
			if(toDisplay.size()>0){
				sga.initiatePopup(toDisplay);
			}
		}
	}
	LinkedList<String> getInfo(int lat, int lon, int latmargin, int lonmargin){
		sga.status_lock.lock();
		sga.location_lock.lock();
		sga.messages_lock.lock();
		LinkedList<String> info = new LinkedList<String>();
		if(isInside(GPSloc, lat, lon, latmargin, lonmargin)){
			String s = "This is your GPS location";
			info.add(s);
		}
		if(isInside(falseLoc, lat, lon, latmargin, lonmargin)){
			String s = "You defined this as your location.";
			info.add(s);
		}
		if(isInside(msgLoc, lat, lon, latmargin, lonmargin)){
			String s = "You defined this as a message location";
			info.add(s);
		}
		
		Set<String> ids = sga.members_location.keySet();
		for(String id : ids){
			if(isInside(sga.members_location.get(id), lat, lon, latmargin, lonmargin)){
				String s = feed.userForGlobalId(id).getName()+" is here.";
				info.add(s);
			}
		}
		for(Message msg : sga.messages){
			if(isInside(msg.point, lat, lon, latmargin, lonmargin)){
				String s = msg.name+" : "+msg.msg;
				info.add(s);
			}
		}
		sga.messages_lock.unlock();
		sga.location_lock.unlock();
		sga.status_lock.unlock();
		return info;
	}
	private boolean isInside(GeoPoint point, int lat, int lon, int latmargin, int lonmargin){
		if(Math.abs(point.getLatitudeE6()-lat) > latmargin){
			return false;
		}
		if(Math.abs(point.getLongitudeE6()-lon) > lonmargin){
			return false;
		}
		return true;
	}
	public void updateMsg(int lat, int lon){
		msgLoc = new GeoPoint(lat, lon);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("latMsg"+id, lat);
		editor.putLong("lonMsg"+id, lon);
		editor.putBoolean("hasMsg"+id, true);
		editor.commit();
		redraw();
	}
	public GeoPoint locToPoint(Location loc){
		GeoPoint point = new GeoPoint(
				(int) (loc.getLatitude() * 1E6), 
				(int) (loc.getLongitude() * 1E6));
		return point;
	}
	public void redraw(){		
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(overlay);
		
	}
	public void sendMessageAtLocation(String msg,boolean isMyLoc){
		GeoPoint toUse = GPSloc;
		if(isMyLoc){
			if(settings.getBoolean("sharing"+id,false) && settings.getLong("locationMode"+id, 0)==1){
				toUse = falseLoc;
			}
		}
		else{
			toUse = msgLoc;
		}
		String loc = feed.getLocalUser().getId()+":"+toUse.getLatitudeE6()+":"+toUse.getLongitudeE6();			
		JSONObject meta = new JSONObject();
		try {
			meta.put(Obj.TYPE_TEXT, loc);
			meta.put(Obj.FIELD_HTML, msg);
			feed.insert(new  MemObj(SharedGPSActivity.TYPE_MUSUGPS_MSG, meta ));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

}
