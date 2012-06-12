package musuGPS.main;

import java.util.Set;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MusuMapOverlay extends Overlay {	
	private Resources res;	
	private MapTouchListener mtl;
	public MusuMapOverlay(Resources r, MapTouchListener mtl){
		res = r;		
		this.mtl = mtl;
		
	}
	  
	private void drawPoint(Canvas canvas,GeoPoint pointToDraw,MapView mapView,int id){		
		Point screenPts = new Point();
	    mapView.getProjection().toPixels(pointToDraw, screenPts);
	    Bitmap bmp = BitmapFactory.decodeResource(res, id);
	    canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null);   
	}
	
	  @Override
	  public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	    super.draw(canvas, mapView, shadow);          
	   mtl.sga.location_lock.lock();
	   mtl.sga.messages_lock.lock();
	       
	    	for(Message msg : mtl.sga.messages){
	    		drawPoint(canvas, msg.point, mapView, R.drawable.pin);
	    	}


	    	Set<String> ids = mtl.sga.members_location.keySet();
	    	for(String id : ids){
	    		drawPoint(canvas, mtl.sga.members_location.get(id), mapView, R.drawable.position);	    	
	    	}


	    	if(mtl.hasMsg){
	    		drawPoint(canvas,mtl.msgLoc,mapView,R.drawable.message);
	    	} 
	    	if(mtl.hasGPS) {
	    		drawPoint(canvas, mtl.GPSloc, mapView, R.drawable.gps);
	    	}
	    	if(mtl.hasFalse){
	    		drawPoint(canvas, mtl.falseLoc, mapView, R.drawable.position);
	    	}
	    	mtl.sga.messages_lock.unlock();
	    	mtl.sga.location_lock.unlock();
	    
	    return true;
	  }
	  
	  public boolean onTap(GeoPoint p, MapView mv){
		  int latmargin = 40*(mv.getLatitudeSpan()/mv.getHeight());
		  int lonmargin = 40*(mv.getLongitudeSpan()/mv.getWidth());		  
		  mtl.update(p.getLatitudeE6(), p.getLongitudeE6(),latmargin,lonmargin);		  
		  return false;
	  }
}
