package musuGPS.main;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class EditSpinnerOnItemSelected implements OnItemSelectedListener{
	MapTouchListener mtl;
	public EditSpinnerOnItemSelected(MapTouchListener mtl){
		this.mtl = mtl;
	}
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		mtl.mode = pos;		
	}
	public void onNothingSelected(AdapterView<?> arg0) {
		mtl.mode = 0;
		
	}

}
