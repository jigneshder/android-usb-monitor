package com.dynamsoft.usbdevice;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.dynamsoft.utils.Logger;

public class MainActivity extends Activity {
	private TextView mInfo;
	private Logger mLogger;
	private HashMap<UsbDevice, UsbDataBinder> mHashMap = new HashMap<UsbDevice, UsbDataBinder>();
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mInfo = (TextView)findViewById(R.id.log);
		
		mLogger = new Logger(this);
		mLogger.setMode(Logger.MODE_TOAST);
		
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		
		usbConnection();
	}
	
	private void usbConnection() {
		IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		registerReceiver(mUsbAttachReceiver , filter);
		filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbDetachReceiver , filter);
		
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		
		showDevices();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUsbDetachReceiver);
		unregisterReceiver(mUsbAttachReceiver);
		unregisterReceiver(mUsbReceiver);
	};

	BroadcastReceiver mUsbDetachReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction(); 

	      if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
	            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	            if (device != null) {
	                // call your method that cleans up and closes communication with the device
	            	UsbDataBinder binder = mHashMap.get(device);
	            	if (binder != null) {
	            		binder.onDestroy();
		            	mHashMap.remove(device);
	            	}
	            }
	        }
	    }
	};
	
	BroadcastReceiver mUsbAttachReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction(); 

	      if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
	    	  showDevices();
	      }
	    }
	};
	
	private static final String ACTION_USB_PERMISSION = "com.dynamsoft.USB_PERMISSION";
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							// call method to set up device communication
							 UsbDataBinder binder = new UsbDataBinder(mUsbManager, device);
							 mHashMap.put(device, binder);
						}
					} else {
						// Log.d(TAG, "permission denied for device " + device);
					}
				}
			}
		}
	};
	
	private void showDevices() {
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while(deviceIterator.hasNext()){
		    UsbDevice device = deviceIterator.next();
		    mUsbManager.requestPermission(device, mPermissionIntent);
		    //your code
		    mLogger.log("usb", "name: " + device.getDeviceName() + ", " +
		    		"ID: " + device.getDeviceId());
		    mInfo.append(device.getDeviceName() + "\n");
		    mInfo.append(device.getDeviceId() + "\n");
		    mInfo.append(device.getDeviceProtocol() + "\n");
		    mInfo.append(device.getProductId() + "\n");
		    mInfo.append(device.getVendorId() + "\n");
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}
}
