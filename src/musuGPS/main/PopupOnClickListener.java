package musuGPS.main;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

public class PopupOnClickListener implements OnClickListener {
	PopupWindow pw;

	public PopupOnClickListener(PopupWindow pw){
		this.pw = pw;
	}
	public void onClick(View v) {
		pw.dismiss();

	}

}
