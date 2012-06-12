package musuGPS.main;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.TextView;

public class MusuGPSActivity extends MapActivity {
    /** Called when the activity is first created. */
	private MapView mapView;
	private MapController mapController;
	private LocationManager locationManager;
	private LocationListener locationListener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        mapView = (MapView) findViewById(R.id.mapView);      
        
        mapView.setStreetView(true);
        
        mapView.setBuiltInZoomControls(true);
        
        mapController = mapView.getController();
        mapController.setZoom(16);
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);  
        Resources r = getResources();
        locationListener = new GPSLocationListener(mapController,mapView, r);
        
        locationManager.requestLocationUpdates(
          LocationManager.GPS_PROVIDER, 
          0, 
          0, 
          locationListener);
        
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation == null){
        	lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if(lastKnownLocation != null){
        	GeoPoint point = new GeoPoint(
  		          (int) (lastKnownLocation.getLatitude() * 1E6), 
  		          (int) (lastKnownLocation.getLongitude() * 1E6));

        	mapController.animateTo(point);
        	mapController.setZoom(16);

        	// add marker

        	MapOverlay mapOverlay = new MapOverlay(r);
        	mapOverlay.setPointToDraw(point);
        	List<Overlay> listOfOverlays = mapView.getOverlays();
        	listOfOverlays.clear();
        	listOfOverlays.add(mapOverlay);
        }
        
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}