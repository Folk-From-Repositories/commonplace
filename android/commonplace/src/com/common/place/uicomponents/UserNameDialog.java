package com.common.place.uicomponents;

import com.common.place.R;
import com.common.place.util.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserNameDialog extends Dialog{

    private Button confirmButton;
    private EditText txt_user_name;
    private Context context;
    
	public UserNameDialog(Context context) {
        super(context , android.R.style.Theme_Translucent_NoTitleBar);
        this.context = context;
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();    
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
         
        setContentView(R.layout.dialog_user_name);
         
        setLayout();
        setClickListener(listener);
    }
     
    private void setClickListener(View.OnClickListener singleListener){
    	confirmButton.setOnClickListener(singleListener);
    }
    
    private void setLayout(){
        confirmButton = (Button) findViewById(R.id.btn_user_name_confirm);
        txt_user_name = (EditText) findViewById(R.id.input_user_name);
    }
    
    View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String newName = txt_user_name.getText().toString();
			if(newName == null || newName.equals("")){
				Toast.makeText(context, "사용자 이름을 입력하세요", Toast.LENGTH_SHORT).show();
				return;
			}
			Utils.setUserName(context, txt_user_name.getText().toString());
			dismiss();
		}
	};
}
