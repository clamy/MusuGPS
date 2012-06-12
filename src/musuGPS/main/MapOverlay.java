package musuGPS.main;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapOverlay extends Overlay {
	private GeoPoint pointToDraw;
	private Resources res;
	public MapOverlay(Resources r){
		res = r;
	}
	  public void setPointToDraw(GeoPoint point) {
	    pointToDraw = point;
	  }

	  public GeoPoint getPointToDraw() {
	    return pointToDraw;
	  }
	  
	  @Override
	  public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	    super.draw(canvas, mapView, shadow);           

	    // convert point to pixels
	    Point screenPts = new Point();
	    mapView.getProjection().toPixels(pointToDraw, screenPts);

	    // add marker
	    Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.button);
	    canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 24, null);    
	    return true;
	  }
}
