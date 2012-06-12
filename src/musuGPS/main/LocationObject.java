package musuGPS.main;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;


public class LocationObject {
	private static final String TYPE = "loc";
	private static final String COORD_LAT = "lat";
	private static final String COORD_LONG = "lon";
	
	public static DbObj from(Location location,Musubi musubi){
		return null;
		//return new DbObj(musubi, "musuGPS", TYPE, name, json(location), localId, hash, raw, senderId, feedId, intKey, timestamp);	
	}
	
	public static JSONObject json(Location location){
        JSONObject obj = new JSONObject();
        try{
            obj.put(COORD_LAT, location.getLatitude());
            obj.put(COORD_LONG, location.getLongitude());
        }catch(JSONException e){}
        return obj;
    }

}
