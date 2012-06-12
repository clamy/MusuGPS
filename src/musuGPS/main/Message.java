package musuGPS.main;

import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Message {
	GeoPoint point = null;
	String name = null;
	String msg = null;
	
	public Message(String db, DbFeed feed){
		Log.e("msg","db "+db);
		try {
			JSONObject meta = new JSONObject(db);
			String info = meta.getString(Obj.TYPE_TEXT);
			msg = meta.getString(Obj.FIELD_HTML);
			if(info != null && msg != null){
				String[] split = info.split(":");
				if(split.length == 3){
					Log.e("msg","Good size");
					name = feed.userForGlobalId(split[0]).getName();
					Log.e("msg","name "+name);
					int lat = Integer.parseInt(split[1]);
					int lon = Integer.parseInt(split[2]);
					Log.e("msg","at "+lat+" "+lon);
					point = new GeoPoint(lat, lon);					
				}
				else{
					Log.e("msg","Wrong size "+split.length);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
