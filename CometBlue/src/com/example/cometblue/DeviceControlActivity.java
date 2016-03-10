package com.example.cometblue;


import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;



import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.widget.EditText;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.ImageView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SwitchCompat;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView tActField;
    private TextView tTgtField;
    private TextView batteryField;
    private TextView statusField;
    private TextView economyField;
    private TextView comfortField;
    private TextView  tView;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private SeekBar seekBar;

    private SwitchCompat mySwitch;

    final Context context = this;
   
	private EditText editTextMainScreen;
	
	//zapisywanie pinu
	
	private SharedPreferences preferences;
	
	private static final String PREFERENCES_NAME = "macAddr";
    private static final String PREFERENCES_TEXT_FIELD = "pin";
    
    //zmienna do odczytu charakterystyk
    private String charValueStr;
    private String statusStr;
    private int mState = 0;
    private double tAct = 0;
    private double tTgt = 0;
    private double tLow= 0;
    private double tHigh= 0;
    private double tOffset= 0;
    private int batteryLevel= 0;
    private int tTgtSet = 0;
    private int tManual = 0;
    private int statusB1 = 0;
    private int statusB2 = 0;
    private int statusB3 = 0;

      private SpeedometerView speedometer;
      private ImageView icBattery;

    

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.w(TAG, "odbior: " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
              
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            	 restorePin();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
              
            	//Log.w(TAG, "ttttttttttttttttt2: ");
            	charValueStr=intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                if (mState==0){
                	
                	String sampleString = charValueStr;
                    String[] stringArray = sampleString.split(":");
                    double[] tempArray = new double[stringArray.length];
                    
                    for (int i = 0; i < stringArray.length; i++) {
                       String numberAsString = stringArray[i];
                       Log.w(TAG, "element: "+ numberAsString);
                       tempArray[i] = Double.parseDouble(numberAsString);
                      
                      // Log.w(TAG, "ttttttttttttttttt3: ");
                    }
                    tAct = tempArray[0];
                    tTgt = tempArray[1];
                    tLow= tempArray[2];
                    tHigh= tempArray[3];
                    tOffset= tempArray[4];
                    
                } 
            	if (mState==1){
            		statusStr=charValueStr;
            		String sampleString = charValueStr;
                    String[] stringArray = sampleString.split(" ");
                    int[] tempArray = new int[stringArray.length];
                    
                    for (int i = 0; i < stringArray.length; i++) {
                       String numberAsString = stringArray[i];
                       Log.w(TAG, "element: "+ numberAsString);
                       tempArray[i] = Integer.parseInt(numberAsString);
                      
                     //  Log.w(TAG, "ttttttttttttttttt3: ");
                    }
                    statusB1 = tempArray[0];
                    statusB2 = tempArray[1];
                    statusB3= tempArray[2];
                    
                } 
            	if (mState==2){
            		
            		//batteryLevel=Integer.parseInt(charValueStr);
            		batteryLevel=Integer.parseInt(charValueStr.trim());
            		//Log.w(TAG, "ttttttttttttttttt4: "+charValueStr);
            		
                }
            	//Log.w(TAG, "ttttttttttttttttt5 ");
            	 if (mState<2){
            		 
                 	mState++;
                 	mBluetoothLeService.readComet(mState);

                 } else {
                 	mState=0;
                 	//Log.w(TAG, "ttttttttttttttttt6 ");
                 	displayDataNew();
                 }
                
                
               
            }
            else if (BluetoothLeService.ACTION_NO_READ.equals(action)) {
               // displayData("Z³y pin");
           //
                pinPrompt();
                
            }
        }
    };



    private void clearUI() {
        
     	seekBar.setProgress(0);
    	tActField.setText(R.string.no_data);
    	tTgtField.setText(R.string.no_data);
    	statusField.setText(R.string.no_data);
    	batteryField.setText(R.string.no_data);
    	economyField.setText(R.string.no_data);
    	comfortField.setText(R.string.no_data);
    	tTgtField.setTextColor(Color.parseColor("#727272"));
    	icBattery.setImageResource(R.drawable.battery_0);
    	speedometer.clearColoredRanges();
    	speedometer.setSpeed(0);
   
    	
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_control);
        setupToolbar();
        //pinPrompt();
        
        mySwitch = (SwitchCompat) findViewById(R.id.mySwitch);
        icBattery = (ImageView)findViewById(R.id.ic_battery);
        mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

        	@Override
        	public void onCheckedChanged(CompoundButton buttonView,
        	boolean isChecked) {

        	if (isChecked) {

        		tManual=1;
        	//Toast.makeText(getApplicationContext(), "manual", Toast.LENGTH_SHORT).show();
        	} else {
        		tManual=0;
        	//	Toast.makeText(getApplicationContext(), "auto", Toast.LENGTH_SHORT).show();
        	}

        	}
        });

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        
        
        speedometer = (SpeedometerView) findViewById(R.id.speedometer);
        speedometer.setMaxSpeed(50);
        speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        speedometer.setMaxSpeed(40);
        speedometer.setMajorTickStep(5);
        speedometer.setMinorTicks(4);
      
      //  tView = (TextView) findViewById(R.id.tempView1);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	
        	        double progress=0;  
        	          
        	      @Override
        	       public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
        	            progress = progresValue;
        	            tTgtSet= progresValue+15;
        	            progress= (progress+15)/2;
        	            Log.w(TAG, "suwak= "+progress);
        	            Log.d(TAG, "suwak_org= "+progresValue);
        	            if (progress!=tTgt){
        	            	tTgtField.setTextColor(Color.RED);
        	            }
        	            else{
        	            	tTgtField.setTextColor(Color.parseColor("#727272"));
        	            	//tTgtField.setTextColor(android.R.attr.textColorSecondary);
        	            	//tTgtField.setTextColor(Color.GRAY);
        	            }
        	            	
        	            if (progress==7.5){
        	            	// tView.setText("T.docelowa: "  + "OFF");
        	            	tTgtField.setText("OFF");
        	            }
        	            else if (progress==28.5){
       	            	// tView.setText("T.docelowa: "  + "ON");
        	            	tTgtField.setText("ON");
       	            }
        	            else{
        	            	//tView.setText("T.docelowa: " + String.valueOf(progress) + "\u00B0C");
        	            	
        	            	tTgtField.setText(String.valueOf(progress)+"\u00B0C");
        	            }
        	            
        	         }
        	
        	          @Override
        	       public void onStartTrackingTouch(SeekBar seekBar) {
        	        }
        	
        	          @Override
        	       public void onStopTrackingTouch(SeekBar seekBar) {
        	        	  
        	        }

        });
          
        
        
        
        
       

        
        
        tActField = (TextView) findViewById(R.id.takt_value);
        tTgtField = (TextView) findViewById(R.id.ttgt_value);
        economyField = (TextView) findViewById(R.id.economy_value);
        comfortField = (TextView) findViewById(R.id.comfort_value);
        batteryField = (TextView) findViewById(R.id.battery_value);
        statusField = (TextView) findViewById(R.id.status_value);

       // getActionBar().setTitle(mDeviceName);
      //  getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        
       
        
        preferences = getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
        
        
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_info:
           	 Toast.makeText(this, R.string.info, Toast.LENGTH_LONG).show();
   	           
               break;
            case R.id.action_refresh:
            	 if(mBluetoothLeService != null) {
                 	Log.d(TAG, "mState=" + mState);
                 	mBluetoothLeService.readComet(mState);
                 }
   	           
               break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }


    private void displayDataNew() {
    	//Log.w(TAG, "ttttttttttttttttt7 ");
       
        		//mDataField.setText(String.valueOf(tAct+tOffset)+"\u00B0C ("+String.valueOf(tAct)+"\u00B0C)");
    			tActField.setText(String.valueOf(tAct+tOffset)+"\u00B0C");
        
    			//tTgtField.setText(String.valueOf(tTgt)+"\u00B0C");
    			
    			 if (tTgt==7.5){
    				 tTgtField.setText("OFF");
	            }
	             else if (tTgt==28.5){
	            	 tTgtField.setText("ON");
	            
	            }
    			 
	             else tTgtField.setText(String.valueOf(tTgt)+"\u00B0C");
    			 tTgtField.setTextColor(Color.parseColor("#727272"));
    			
    			economyField.setText(String.valueOf(tLow)+"\u00B0C");
    			comfortField.setText(String.valueOf(tHigh)+"\u00B0C");
    			
    			statusField.setText(statusB1+":"+statusB2+":"+statusB3);
    			
    			if(statusB1 == 1) { 
    				mySwitch.setChecked(true);
    			}
    			else mySwitch.setChecked(false);
    			
    			batteryField.setText(String.valueOf(batteryLevel)+"%");
    			
    			
    			if(batteryLevel >= 80) { 
    				icBattery.setImageResource(R.drawable.battery_100);	
    			}
    			else if(batteryLevel >= 60) { 
    				icBattery.setImageResource(R.drawable.battery_60);
    			}
    			else if(batteryLevel >= 40) { 
    				icBattery.setImageResource(R.drawable.battery_60);
    			}
    			else if(batteryLevel >= 20) { 
    				icBattery.setImageResource(R.drawable.battery_60);
    			}
    			else {
    				icBattery.setImageResource(R.drawable.battery_0);
        		}
    			
        		
        		speedometer.setSpeed(tAct+tOffset, 1000, 300);
        		speedometer.addColoredRange(0, tLow, getResources().getColor(R.color.range_green));
                speedometer.addColoredRange(tLow, tHigh, getResources().getColor(R.color.range_yellow));
                speedometer.addColoredRange(tHigh, 40, getResources().getColor(R.color.range_red));
            
                double x=tTgt*2;
                int i = (int) x-15;
                seekBar.setProgress(i);
        
    }
   
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_NO_READ);
        return intentFilter;
    }


    public void onClickWritePin(int pin){
        if(mBluetoothLeService != null) {
        	Log.d(TAG, "pin_write_po_restore=" + pin);
            mBluetoothLeService.writeCustomCharacteristic(pin);
        }
    }

    public void onClickRead(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.readCustomCharacteristic();
        }
    }
    public void onClickComet(View v){
        if(mBluetoothLeService != null) {
        	Log.d(TAG, "mState=" + mState);
        	mBluetoothLeService.readComet(mState);
        }
        
    }
    
    public void onClickSet(View v){
        if(mBluetoothLeService != null) {
        	Log.d(TAG, "tTgtSet=" + tTgtSet);
        	int [ ] valWrite = {tTgtSet, tManual};
        	mBluetoothLeService.valueToWrite(valWrite);
        }
        
    }
    
    public void pinPrompt() {

		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(context);

		View promptView = layoutInflater.inflate(R.layout.pin, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);

		// set prompts.xml to be the layout file of the alertdialog builder
		alertDialogBuilder.setView(promptView);

		final EditText input = (EditText) promptView.findViewById(R.id.userInput);

		// setup a dialog window
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// get user input and set it to result
								//editTextMainScreen.setText(input.getText());
								String val = input.getText().toString();
								int value = Integer.valueOf(val);
								savePin(val);
								onClickWritePin(value);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,	int id) {
								dialog.cancel();
							}
						});

		// create an alert dialog
		AlertDialog alertD = alertDialogBuilder.create();

		alertD.show();

	}
    private void savePin(String val) {
		SharedPreferences.Editor preferencesEditor = preferences.edit();
		String editTextData = val;
		preferencesEditor.putString(PREFERENCES_TEXT_FIELD, editTextData);
		preferencesEditor.commit();
	}
		
	private void restorePin() {
		String textFromPreferences = preferences.getString(PREFERENCES_TEXT_FIELD, "");
		//editTextMainScreen.setText(textFromPreferences);
		Log.d(TAG, "pin_str=" +textFromPreferences);
		int value=0;
		 if (textFromPreferences != ""){
		
		 value = Integer.valueOf(textFromPreferences);
		
		 }
		 Log.d(TAG, "odczytpin=" + value);
		onClickWritePin(value);
		
	} 
	 private void setupToolbar(){
	        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	        setSupportActionBar(toolbar);
	    // Show menu icon
	        final ActionBar ab = getSupportActionBar();
	        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
	        ab.setDisplayHomeAsUpEnabled(true);
	    }
		
}
