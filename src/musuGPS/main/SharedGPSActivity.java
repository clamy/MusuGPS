package musuGPS.main;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;
import musuGPS.main.R;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;

public class SharedGPSActivity extends MapActivity {
	Musubi mMusubi;
	boolean sharingGPS = false;
	Button button;
	DbFeed feed;
	private LocationManager locationManager;
	private LocationListener locationListener;
	public static final String TYPE_MUSUGPS = "musuGPS";
	public static final String TYPE_MUSUGPS_POSITION = "musuGPSPos";
	public static final String TYPE_MUSUGPS_MSG = "musuGPSMessage";
	public static final String TYPE_MUSUGPS_STATUS = "musuGPSStatus";	
	public HashMap<String,Boolean> is_sharing = new HashMap<String, Boolean>();
	public HashMap<String,GeoPoint> members_location = new HashMap<String, GeoPoint>();
	private Cursor member_status;
	private Cursor member_location;
	private Cursor all_messages;
	public ReentrantLock messages_lock = new ReentrantLock();
	public ReentrantLock location_lock = new ReentrantLock();
	public ReentrantLock status_lock = new ReentrantLock();
	private int status_id;
	private int location_id;
	private int message_id;
	public LinkedList<Message> messages = new LinkedList<Message>();
	private int nsharing = 0;
	private UpdateThread updater;
	private MapTouchListener mtl;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		mMusubi = Musubi.forIntent(this, getIntent());
		setContentView(R.layout.musubi);
		feed = mMusubi.getFeed();		
		
		ExpandableListView list = (ExpandableListView) findViewById(R.id.expandableListView1);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		MapView mapView = (MapView) findViewById(R.id.mapView);		
		mapView.setStreetView(true);
		mapView.setBuiltInZoomControls(true);

		MapController mapController = mapView.getController();
		mapController.setZoom(16);
		
		getMemberStatus(0);
		getMemberLocation(0);
		getMessages(0);
		
		mtl = new MapTouchListener(mapController, locationManager, getResources(), mapView, getPreferences(MODE_APPEND), feed,this,getBaseContext());
		
		MyListAdapter mla = new MyListAdapter(getBaseContext(),(int) feed.getLocalId(),getPreferences(MODE_APPEND),feed,locationManager,mtl);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.edit_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new EditSpinnerOnItemSelected(mtl));
		
		list.setAdapter(mla);
		list.setOnGroupCollapseListener(mla);
		updater = new UpdateThread(this);
		Thread tu = new Thread(updater);		
		tu.start();
		
	}
	
	
	private String JSONToHtml(String JSON){
		String html = JSON.substring(11, JSON.length()-2);
		return html;
	}
	private void getMemberStatus(int minId){
		status_lock.lock();
		String selection = "type = ? AND _id > ?";
        String[] selectionArgs = new String[] { TYPE_MUSUGPS_STATUS ,""+minId};
        member_status = feed.query(selection, selectionArgs);        
        member_status.moveToFirst();
        boolean do_update = true;
        while(!member_status.isAfterLast()&&!member_status.isBeforeFirst()){ 
        	if(do_update){
        		do_update = false;        		
        		status_id = member_status.getInt(0);        		
        	}
        	if(member_status.getInt(0)>minId){
        		String status = member_status.getString(5);
        		Log.e("db-status", status);
        		try {
					JSONObject meta = new JSONObject(status);
					String identity = meta.getString(Obj.TYPE_TEXT);
					String status_skimmed = meta.getString(Obj.FIELD_HTML);


					if(identity != null && status_skimmed != null){  
						if(!is_sharing.containsKey(identity)){
							Log.e("db","I have message "+status_skimmed+" from "+feed.userForGlobalId(identity).getName());
							if(status_skimmed.equals("<body>I started sharing my location</body>")){
								is_sharing.put(identity, true);
								Log.e("db","I have one member sharing "+feed.userForGlobalId(identity).getName());
								nsharing ++;
							}
							else{
								is_sharing.put(identity, false);
								if(members_location.containsKey(identity)){
									members_location.remove(identity);
								}
							}
						}
					}
        		} catch (JSONException e) {					
        			e.printStackTrace();
        		}
        	}
        	member_status.moveToNext();
        }
        member_status.close();
        status_lock.unlock();
	}
	
	private void getMemberLocation(int minId){
		status_lock.lock();
		location_lock.lock();
		String selection = "type = ? AND _id > ?";
        String[] selectionArgs = new String[] { TYPE_MUSUGPS_POSITION, ""+minId };
        member_location = feed.query(selection, selectionArgs);        
        member_location.moveToFirst();
        int nfound = 0;
        boolean do_update = true;
        HashSet<String> already_found = new HashSet<String>();
        while(!member_location.isAfterLast()&&!member_location.isBeforeFirst()&&nfound <nsharing){ 
        	if(do_update){
        		do_update = false;        		
        		location_id = member_location.getInt(0);        		
        	}
        	if(member_location.getInt(0)>minId){
        		String location = member_location.getString(5);
        		Log.e("db", location);
        		String location_skimmed = JSONToHtml(location);
        		String split[] = location_skimmed.split(":");
        		Log.e("db","Skimmed is "+location_skimmed+" and split gave "+split.length);
        		if(split.length == 3){        		
        			String identity = split[0];
        			if(is_sharing.containsKey(identity)&&!already_found.contains(identity)){
        				if(is_sharing.get(identity)){
        					int lat = Integer.parseInt(split[1]);
        					int lon = Integer.parseInt(split[2]);
        					Log.e("db","I have "+feed.userForGlobalId(identity).getName()+" at "+lat+" and "+lon);
        					nfound ++;
        					members_location.put(identity, new GeoPoint(lat, lon));
        					already_found.add(identity);
        				}
        			}
        		}
        	}
        	member_location.moveToNext();
        }
        member_location.close();
        location_lock.unlock();
        status_lock.unlock();
	}

	private void getMessages(int minId){
		messages_lock.lock();
		String selection = "type = ? AND _id > ?";
        String[] selectionArgs = new String[] { TYPE_MUSUGPS_MSG, ""+minId };
        all_messages = feed.query(selection, selectionArgs);
        all_messages.moveToFirst();
        boolean do_update = true;        
        while(!all_messages.isAfterLast()&&!all_messages.isBeforeFirst()){
        	if(do_update){
        		do_update = false;        		
        		message_id = all_messages.getInt(0);        		
        	}
        	if(all_messages.getInt(0)>minId){
        		String msg = all_messages.getString(5);      	
        		
        		Log.e("msg",msg);
        		Message m = new Message(msg,feed);
        		if(m.msg != null && m.name != null & m.point != null){
        			messages.add(m);  
        		}
        	}
        	all_messages.moveToNext();
        }
        all_messages.close();
        messages_lock.unlock();
	}
	
	
	@Override
	protected boolean isRouteDisplayed() {		
		return false;
	}
	
	void initiatePopup(LinkedList<String> toDisplay){
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.message_popup,(ViewGroup) findViewById(R.id.popup_layout));
		PopupWindow pw = new PopupWindow(layout, 300, 470, true);
		pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
		Button cancelButton = (Button) layout.findViewById(R.id.popup_done);
		cancelButton.setOnClickListener(new PopupOnClickListener(pw));
		ListView lv = (ListView) layout.findViewById(R.id.popup_list);		
		ArrayAdapter<String> arrayAd = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_list_item_1,android.R.id.text1,toDisplay);
		lv.setAdapter(arrayAd);
		
	}
	public void updateFeed(){
		getMemberStatus(status_id);
		getMemberLocation(location_id);
		getMessages(message_id);
		mtl.updateGPS();
        mtl.redraw();
	}
	protected void finalize(){
		updater.stop();		
	}
	
}

