package musuGPS.main;

import android.content.SharedPreferences;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class RadioListener implements OnCheckedChangeListener{
	SharedPreferences settings;
	int lastValue;
	Boolean isShare;
	int id;
	
	public RadioListener(SharedPreferences settings, int start, Boolean isShare,int id){
		this.settings = settings;
		this.lastValue = start;
		this.isShare = isShare;
		this.id = id;
	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if(lastValue == 1){
			lastValue = 0;
		}
		else{
			lastValue = 1;
		}
		if(isShare){
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong("sharedMode"+id,lastValue);
			editor.commit();
		}
		else{
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong("locationMode"+id,lastValue);
			editor.commit();
		}
		
	}

}
