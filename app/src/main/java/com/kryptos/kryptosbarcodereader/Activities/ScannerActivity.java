package com.kryptos.kryptosbarcodereader.Activities;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.kryptos.kryptosbarcodereader.R;
import com.kryptos.kryptosbarcodereader.Scanner.FormatSelectorDialogFragment;
import com.kryptos.kryptosbarcodereader.Scanner.MessageDialogFragment;
import com.kryptos.kryptosbarcodereader.Scanner.MyScannerView;
import com.kryptos.kryptosbarcodereader.Utilities.KryptosPreference;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import eneter.messaging.diagnostic.EneterTrace;
import eneter.messaging.endpoints.typedmessages.DuplexTypedMessagesFactory;
import eneter.messaging.endpoints.typedmessages.IDuplexTypedMessageSender;
import eneter.messaging.endpoints.typedmessages.IDuplexTypedMessagesFactory;
import eneter.messaging.endpoints.typedmessages.TypedResponseReceivedEventArgs;
import eneter.messaging.messagingsystems.messagingsystembase.IDuplexOutputChannel;
import eneter.messaging.messagingsystems.messagingsystembase.IMessagingSystemFactory;
import eneter.messaging.messagingsystems.tcpmessagingsystem.TcpMessagingSystemFactory;
import eneter.net.system.EventHandler;

public class ScannerActivity extends ActionBarActivity implements
		MessageDialogFragment.MessageDialogListener,
		MyScannerView.ResultHandler,
		FormatSelectorDialogFragment.FormatSelectorDialogListener {
	private static final String FLASH_STATE = "FLASH_STATE";
	private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
	private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
	private MyScannerView mScannerView;
	private boolean mFlash;
	private boolean mAutoFocus;
	private ArrayList<Integer> mSelectedIndices;
	private KryptosPreference mySession;

	private Handler myRefresh = new Handler();

	private static final int DELAY = 60000;
	public boolean inactive;

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			if(inactive) {
				finish();
			} else {
				inactive = true;

				mHideHandler.postDelayed(mHideRunnable, DELAY);
			}
		}
	};



	// Request message type
	// The message must have the same name as declared in the service.
	// Also, if the message is the inner class, then it must be static.
	public static class MyRequest {
		public String Text;
	}

	// Response message type
	// The message must have the same name as declared in the service.
	// Also, if the message is the inner class, then it must be static.
	public static class MyResponse {
		public int Length;
	}

	// Sender sending MyRequest and as a response receiving MyResponse.
	private IDuplexTypedMessageSender<MyResponse, MyRequest> mySender;

	private String myPurpose = "";

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		mHideHandler.postDelayed(mHideRunnable, DELAY);

		mySession = new KryptosPreference(ScannerActivity.this);
		if (state != null) {
			mFlash = state.getBoolean(FLASH_STATE, false);
			mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
			mSelectedIndices = state.getIntegerArrayList(SELECTED_FORMATS);
		} else {
			mFlash = false;
			mAutoFocus = true;
			mSelectedIndices = null;
		}

		mScannerView = new MyScannerView(this);
		setupFormats();
		setContentView(mScannerView);

		Intent i = getIntent();

		if (i != null) {

			myPurpose = i.getStringExtra("PURPOSE");

		}

		if (myPurpose != null)
			if (myPurpose.equals("scanner")) {
				// Open the connection in another thread.
				// Note: From Android 3.1 (Honeycomb) or higher
				// it is not possible to open TCP connection
				// from the main thread.
				Thread anOpenConnectionThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							openConnection();
						} catch (Exception err) {
							EneterTrace.error("Open connection failed.", err);
						}
					}
				});
				anOpenConnectionThread.start();

			}

	}

	private void openConnection() throws Exception {
		try {
			// Create sender sending MyRequest and as a response receiving
			// MyResponse
			IDuplexTypedMessagesFactory aSenderFactory = new DuplexTypedMessagesFactory();
			mySender = aSenderFactory.createDuplexTypedMessageSender(
                    MyResponse.class, MyRequest.class);

			// Subscribe to receive response messages.
			mySender.responseReceived().subscribe(myOnResponseHandler);

			// Create TCP messaging for the communication.
			// Note: 10.0.2.2 is a special alias to the loopback (127.0.0.1)
			// on the development machine.
			IMessagingSystemFactory aMessaging = new TcpMessagingSystemFactory();
			IDuplexOutputChannel anOutputChannel
                    // = aMessaging.createDuplexOutputChannel("tcp://10.0.2.2:8060/");
                    = aMessaging.createDuplexOutputChannel(mySession.getIpConfig());

			// Attach the output channel to the sender and be able to send
			// messages and receive responses.
			mySender.attachDuplexOutputChannel(anOutputChannel);

		} catch (SocketTimeoutException e){


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private EventHandler<TypedResponseReceivedEventArgs<MyResponse>> myOnResponseHandler = new EventHandler<TypedResponseReceivedEventArgs<MyResponse>>() {
		@Override
		public void onEvent(Object sender,
							TypedResponseReceivedEventArgs<MyResponse> e) {
			onResponseReceived(sender, e);
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myPurpose.equals("scanner")) {
			// Stop listening to response messages.
			mySender.detachDuplexOutputChannel();

		}
	}

	private void onSendRequest(String aMessage) {
		// Create the request message.
		final MyRequest aRequestMsg = new MyRequest();
		aRequestMsg.Text = aMessage;

		// Send the request message.
		try {
			mySender.sendRequestMessage(aRequestMsg);
		} catch (IllegalStateException e){

			EneterTrace.error("Sending the message failed.", e);

		} catch (Exception err) {
			EneterTrace.error("Sending the message failed.", err);
		}

	}

	private void onResponseReceived(Object sender,
									final TypedResponseReceivedEventArgs<MyResponse> e) {
		// Display the result - returned number of characters.
		// Note: displaying to the correct UI thread.
		myRefresh.post(new Runnable() {
			@Override
			public void run() {
				// myResponseEditText.setText(Integer.toString(e.getResponseMessage().Length));
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		mScannerView.setResultHandler(this);
		mScannerView.startCamera();
		mScannerView.setFlash(mFlash);
		mScannerView.setAutoFocus(mAutoFocus);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(FLASH_STATE, mFlash);
		outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
		outState.putIntegerArrayList(SELECTED_FORMATS, mSelectedIndices);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem;

		if (mFlash) {
			menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0,
					R.string.flash_on);
		} else {
			menuItem = menu.add(Menu.NONE, R.id.menu_flash, 0,
					R.string.flash_off);
		}
		MenuItemCompat
				.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

		if (mAutoFocus) {
			menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0,
					R.string.auto_focus_on);
		} else {
			menuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0,
					R.string.auto_focus_off);
		}
		MenuItemCompat
				.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

		menuItem = menu.add(Menu.NONE, R.id.menu_formats, 0, R.string.formats);
		MenuItemCompat
				.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.menu_flash:
				mFlash = !mFlash;
				if (mFlash) {
					item.setTitle(R.string.flash_on);
				} else {
					item.setTitle(R.string.flash_off);
				}
				mScannerView.setFlash(mFlash);
				return true;
			case R.id.menu_auto_focus:
				mAutoFocus = !mAutoFocus;
				if (mAutoFocus) {
					item.setTitle(R.string.auto_focus_on);
				} else {
					item.setTitle(R.string.auto_focus_off);
				}
				mScannerView.setAutoFocus(mAutoFocus);
				return true;
			case R.id.menu_formats:
				DialogFragment fragment = FormatSelectorDialogFragment.newInstance(
						this, mSelectedIndices);
				fragment.show(getSupportFragmentManager(), "format_selector");
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void handleResult(Result rawResult) {

		inactive = false;

		try {
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);
			r.play();
		} catch (IllegalStateException e){

		} catch (Exception e) {
		}
		if (myPurpose.equals("scanner")) {

			// Data is sent from here

			onSendRequest("\\"+rawResult.getText()+ "\\");

			inactive = false;

		} else {
			Intent i = new Intent(ScannerActivity.this, SettingsActivity.class);
			i.putExtra("IP_CONFIG", rawResult.getText().toString());
			startActivity(i);
			this.finish();

		}
		showMessageDialog("Contents = " + rawResult.getText() + ", Format = "
				+ rawResult.getBarcodeFormat().toString());
	}

	public void showMessageDialog(String message) {
		Toast.makeText(ScannerActivity.this, message, Toast.LENGTH_SHORT)
				.show();

		DialogFragment fragment = MessageDialogFragment.newInstance(
				"Scan Results", message, this);
		fragment.show(getSupportFragmentManager(), "scan_results");
	}

	public void closeMessageDialog() {
		closeDialog("scan_results");
	}

	public void closeFormatsDialog() {
		closeDialog("format_selector");
	}

	public void closeDialog(String dialogName) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		DialogFragment fragment = (DialogFragment) fragmentManager
				.findFragmentByTag(dialogName);
		if (fragment != null) {
			fragment.dismiss();
		}
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// Resume the camera
		mScannerView.startCamera();
		mScannerView.setFlash(mFlash);
		mScannerView.setAutoFocus(mAutoFocus);
	}

	@Override
	public void onFormatsSaved(ArrayList<Integer> selectedIndices) {
		mSelectedIndices = selectedIndices;
		setupFormats();
	}

	public void setupFormats() {
		List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
		if (mSelectedIndices == null || mSelectedIndices.isEmpty()) {
			mSelectedIndices = new ArrayList<Integer>();
			for (int i = 0; i < MyScannerView.ALL_FORMATS.size(); i++) {
				mSelectedIndices.add(i);
			}
		}

		for (int index : mSelectedIndices) {
			formats.add(MyScannerView.ALL_FORMATS.get(index));
		}
		if (mScannerView != null) {
			mScannerView.setFormats(formats);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();
		closeMessageDialog();
		closeFormatsDialog();
	}
}