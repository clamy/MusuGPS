package musuGPS.main;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class GPSLocationListener implements LocationListener{
	private MapController mapController;
	private MapView mapView;
	private Resources res;
	public GPSLocationListener(MapController mc,MapView mv,Resources r){
		mapController = mc;
		mapView = mv;
		res = r;
		
	}
	public void onLocationChanged(Location location) {
		if (location != null) {
		      GeoPoint point = new GeoPoint(
		          (int) (location.getLatitude() * 1E6), 
		          (int) (location.getLongitude() * 1E6));
		      
		      mapController.animateTo(point);
		      mapController.setZoom(16);
		      
		      // add marker
		      
		      MapOverlay mapOverlay = new MapOverlay(res);
		      mapOverlay.setPointToDraw(point);
		      List<Overlay> listOfOverlays = mapView.getOverlays();
		      listOfOverlays.clear();
		      listOfOverlays.add(mapOverlay);
		}
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
