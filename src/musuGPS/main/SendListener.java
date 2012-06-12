package musuGPS.main;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SendListener implements OnClickListener {
	MapTouchListener mtl;
	RadioButton rg;
	EditText text;
	public SendListener(MapTouchListener mtl,RadioButton rg, EditText text){
		this.mtl = mtl;
		this.rg = rg;
		this.text = text;
	}
	public void onClick(View arg0) {
		Editable edit = text.getText();			
		String msg = edit.toString();
		mtl.sendMessageAtLocation(msg, rg.isChecked());
		text.setText("");
	}

}
