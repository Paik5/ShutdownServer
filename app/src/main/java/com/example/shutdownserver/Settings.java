package com.example.shutdownserver;


import java.io.IOException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Settings extends Shutdown {
	
	private EditText editTextHostname;
	private EditText editTextPort;
	private EditText editTextMac;
	
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		editTextHostname = (EditText) findViewById(R.id.editTextHostname);
		editTextPort = (EditText) findViewById(R.id.editTextPort);
		editTextMac = (EditText) findViewById(R.id.editTextMac);
		String dataReturnedhost = host.getString("hostname", "");
		editTextHostname.setText(dataReturnedhost);
		String dataReturnedport = host.getString("port", "");
		editTextPort.setText(dataReturnedport);
		String dataReturnedmac = host.getString("mac", "");
		editTextMac.setText(dataReturnedmac);
	}

	public void onClickSave (View v) throws IOException
	{
		String hostname = editTextHostname.getText().toString();
		SharedPreferences.Editor editor = host.edit();
		editor.putString("hostname", hostname);
		
		String port = editTextPort.getText().toString();
		editor.putString("port", port);
		
		String mac = editTextMac.getText().toString();
		editor.putString("mac", mac);
		
		editor.commit();
				
		Intent intent = new Intent(this, Shutdown.class);
    	startActivity(intent);
	}
	
	public void onClickBack (View v)
	{
		Intent intent = new Intent(this, Shutdown.class);
		startActivity(intent);
	}
	
	@Override
    public void onBackPressed() {
            super.onBackPressed();
            this.finish();
    }
}
