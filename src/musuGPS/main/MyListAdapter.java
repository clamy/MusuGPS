package musuGPS.main;

import com.google.android.maps.MapView;

import mobisocial.socialkit.musubi.DbFeed;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MyListAdapter extends BaseExpandableListAdapter implements OnGroupCollapseListener{
	
	private String[] groups = {"Share location","Share content"};
	private String[][] children = {{"Sharing mode","Location mode","Currently sharing"},
			{"Location mode","Share"}};
	private Context context;	
	private SharedPreferences settings;
	private int id;
	DbFeed feed;
	LocationManager locMan;	
	MapTouchListener mtl;
	RadioButton toSend;
	public MyListAdapter(Context context, int id,SharedPreferences settings,DbFeed feed,LocationManager locMan, MapTouchListener mtl){
		
		super();
		this.context = context;
		
		this.settings = settings;
		this.id = id;
		this.feed = feed;
		this.locMan = locMan;
		this.mtl = mtl;
		
	}
	
	public Object getChild(int arg0, int arg1) {
		
		return children[arg0][arg1];
	}

	
	public long getChildId(int groupPosition, int childPosition) {
		
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		
        LinearLayout lay = new LinearLayout(context);
        if((groupPosition == 0 && childPosition != 2 && childPosition != 3) || (groupPosition == 1 && childPosition != 2)){
        	lay.setOrientation(LinearLayout.VERTICAL);
        	if(groupPosition == 0 || childPosition == 0 ){
        		CheckedTextView textView = getGenericView(getChild(groupPosition, childPosition).toString(),true);
        		lay.addView(textView);
        	}
        }
        else{
        	lay.setOrientation(LinearLayout.HORIZONTAL);        	
        	CheckedTextView textView = getGenericView(getChild(groupPosition, childPosition).toString(),false);
        	lay.addView(textView);
        	
        }        
        if(groupPosition == 0){
        	switch (childPosition){
        	case 0:
        		RadioGroup rg = new RadioGroup(context);
        		RadioButton rb1 = new RadioButton(context);
        		RadioButton rb2 = new RadioButton(context);
        		rb1.setText("Energy saving");        		
        		rb2.setText("Precise");
        		rg.addView(rb1);
        		rg.addView(rb2);
        		rg.setOrientation(RadioGroup.HORIZONTAL);
        		int sharedMode = (int) settings.getLong("sharedMode"+id, 0);
        		if(sharedMode ==0){
        			rb1.setChecked(true);
        		}
        		else{
        			rb2.setChecked(true);
        		}
        		rg.setOnCheckedChangeListener(new RadioListener(settings, sharedMode,true,id));
        		lay.addView(rg);
        		break;
        	case 1:
        		RadioGroup rg2 = new RadioGroup(context);
        		RadioButton rb12 = new RadioButton(context);
        		RadioButton rb22 = new RadioButton(context);
        		rb12.setText("GPS location");
        		rb22.setText("Designed location");
        		rg2.addView(rb12);
        		rg2.addView(rb22);
        		rg2.setOrientation(RadioGroup.HORIZONTAL);
        		int locationMode = (int) settings.getLong("locationMode"+id, 0);
        		if(locationMode==0){
        			rb12.setChecked(true);
        		}
        		else{
        			rb22.setChecked(true);
        		}
        		rg2.setOnCheckedChangeListener(new RadioListener(settings, locationMode, false,id));
        		lay.addView(rg2);
        		break;
        	case 2:
        		CheckBox cb = new CheckBox(context);
        		lay.addView(cb);
        		Boolean sharing = settings.getBoolean("sharing"+id, false);
        		if(sharing){
        			cb.setChecked(true);
        		}
        		cb.setOnClickListener(new SharingListener(settings, sharing,id,feed,locMan,mtl));
        		break;
        	}
        	
        		
        }
        else if(groupPosition == 1){
        	switch(childPosition){
        	case 0:
        		RadioGroup rg3 = new RadioGroup(context);
        		RadioButton rb13 = new RadioButton(context);
        		RadioButton rb23 = new RadioButton(context);
        		rb13.setText("My location");
        		rb23.setText("Designed location");
        		rg3.addView(rb13);
        		rg3.addView(rb23);
        		rg3.setOrientation(RadioGroup.HORIZONTAL);
        		rb13.setChecked(true);
        		lay.addView(rg3);
        		toSend = rb13;
        		break;
        	case 1:
        		LinearLayout lay2 = new LinearLayout(context);
        		CheckedTextView textView = getGenericView(getChild(groupPosition, childPosition).toString(),false);
        		lay2.addView(textView);
        		
        		Button img = new Button(context);
        		img.setText("Add");
        		lay2.addView(img);
        		
        		Button send = new Button(context);
        		send.setText("Send");
        		lay2.addView(send);
        		lay.addView(lay2);
        		EditText text = new EditText(context);
        		text.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        		text.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
        		lay.addView(text);
        		send.setOnClickListener(new SendListener(mtl, toSend, text));
        		break;
        	
        	
        	}
        	
        }
        
		return lay;
	}

	public int getChildrenCount(int groupPosition) {
		
		return children[groupPosition].length;
	}

	public Object getGroup(int groupPosition) {
		
		return groups[groupPosition];
	}

	public int getGroupCount() {
		
		return groups.length;
	}

	public long getGroupId(int groupPosition) {
		
		return groupPosition;
	}

	public CheckedTextView getGenericView(String text,boolean fill) {
        // Layout parameters for the ExpandableListView
		
        AbsListView.LayoutParams lp = null;
        if(fill){
        	lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
        }
        else{
        	lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 64);
        }
        CheckedTextView textView = new CheckedTextView(context);
        textView.setLayoutParams(lp);
        // Center the text vertically
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
        
        // Set the text starting position
        textView.setPadding(36, 0, 0, 0);
        textView.setText(text);
        return textView;
    }
	
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		CheckedTextView textView = getGenericView(getGroup(groupPosition).toString(),true);       
        return textView;
	}

	public boolean hasStableIds() {
		
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void onGroupCollapse(int groupPosition) {		
		
	}

}
