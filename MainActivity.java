package com.github.hrobertson.headtrackudp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import com.github.hrobertson.headtrackudp.R;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
	
	private PowerManager pm;
	private PowerManager.WakeLock wl;
	private SensorManager mSensorManager;
	private String ipAddress;
	private InetAddress receiverAddress;
	private DatagramSocket datagramSocket;
	private int port = 5550;
	private SenderThread senderThread;
		
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
	
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    this.wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HeadTrack");
    this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    EditText ipInput = (EditText) findViewById(R.id.ip_input);
    ipInput.setText("192.168.1.60");
  }
  
	public void toggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((android.widget.ToggleButton) view).isChecked();
		
		if (on) {
			EditText ipInput = (EditText) findViewById(R.id.ip_input);
			ipAddress = ipInput.getText().toString();
			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			try {
				receiverAddress = InetAddress.getByName(ipAddress);
				datagramSocket = new DatagramSocket();
				senderThread = new SenderThread();
				senderThread.start();
					
				registerListeners();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			}
	    
		} else { // Switch off
			unRegisterListeners();
		}
	}

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
  }

	@SuppressWarnings("deprecation")
	private void registerListeners() {
		Log.v("HeadTrack", "registerListeners called");
		wl.acquire();
		Log.v("HeadTrack", "wakelock is held: "+wl.isHeld());
		this.mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 100000);
	}
	
	private void unRegisterListeners() {
		Log.v("HeadTrack", "unRegisterListeners called");
		mSensorManager.unregisterListener(this);
		senderThread.requestStop();
		wl.release();
	}

	@Override
	protected void onResume() {
		Log.v("HeadTrack", "onResume called");
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		Log.v("HeadTrack", "onDestroy called");
		unRegisterListeners();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		Log.v("HeadTrack", "onPause called");
		super.onPause();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}


	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			double data[] = {0,0,0,0,0,0};
			data[3] = (double) event.values[0];
			data[4] = (double) event.values[1];
			data[5] = (double) event.values[2];
			
			ByteBuffer bb = ByteBuffer.allocate(data.length * 8);
			DoubleBuffer db = bb.asDoubleBuffer();
			for (double d : data) db.put(d);
			byte[] databytes = bb.array();
			
			Double dtest = data[3];
			ByteBuffer bbtest = ByteBuffer.allocate(8);
			DoubleBuffer dbtest = bbtest.asDoubleBuffer();
			dbtest.put(dtest);
			byte[] batest = bbtest.array();
			
			String output = "";
			for (int i=0;i<48;i++) {
				output += String.format("%8s", Integer.toBinaryString(databytes[i] & 0xFF)).replace(' ', '0');
			}
			Log.v("HeadTrack", output);
			
			TextView tv;
			Resources res = getResources();
			String binary;
			for (int i=1;i<9;i++) {
				tv = (TextView)findViewById(res.getIdentifier("text"+i, "id", this.getPackageName()));
				binary = String.format("%8s", Integer.toBinaryString(batest[i-1] & 0xFF)).replace(' ', '0');
				tv.setText(binary);
			}

			senderThread.enquePacket(new UDPSender(datagramSocket, receiverAddress, port, databytes));

			setTable(event.values);
		}
	}
	
	private void setTable(float[] values) {
		TextView c1 = (TextView)findViewById(R.id.r1c1);
		TextView c2 = (TextView)findViewById(R.id.r1c2);
		TextView c3 = (TextView)findViewById(R.id.r1c3);
		
		c1.setText(Float.toString(values[0]));
		c2.setText(Float.toString(values[1]));
		c3.setText(Float.toString(values[2]));
	}
}
