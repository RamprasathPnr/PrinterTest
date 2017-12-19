package com.printertest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.lvrenyang.io.IOCallBack;
import com.lvrenyang.io.Pos;
import com.printertest.myprinter.Global;
import com.printertest.myprinter.WorkService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class ConnectUSBActivity extends Activity implements OnClickListener {

	private static Handler mHandler = null;
	private static String TAG = "ConnectUSBActivity";

	private LinearLayout linearLayoutUSBDevices;
	private AlertDialog alertDialog;
	private Button btnPrint,btnPrint1;

	WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connectusb);


		WorkService.cb = new IOCallBack() {
			// WorkThread线程回调
			public void OnOpen() {
				// TODO Auto-generated method stub
				if(null != mHandler)
				{
				/*	Message msg = mHandler.obtainMessage(Global.MSG_IO_ONOPEN);
					mHandler.sendMessage(msg);*/
				}
			}

			public void OnClose() {
				// TODO Auto-generated method stub
				if(null != mHandler)
				{
					Message msg = mHandler.obtainMessage(Global.MSG_IO_ONCLOSE);
					mHandler.sendMessage(msg);
				}
			}

		};
		mHandler = new MHandler(this);
		WorkService.addHandler(mHandler);

		if (null == WorkService.workThread) {
			Intent intent = new Intent(this, WorkService.class);
			startService(intent);
		}


		linearLayoutUSBDevices = (LinearLayout) findViewById(R.id.linearLayoutUSBDevices);
		btnPrint = (Button)findViewById(R.id.Print);
		btnPrint.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent =new Intent(ConnectUSBActivity.this,StartActivity.class);
				startActivity(intent);
				finish();

			}
		});

		/*btnPrint1 =(Button)findViewById(R.id.Print1);
		btnPrint1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//probe1();
			}
		});*/
		/*mHandler = new MHandler(this);
		WorkService.addHandler(mHandler);*/

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			probe();
		} else {
			finish();
		}

		IntentFilter filterDetached = new IntentFilter();
		filterDetached.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filterDetached);

		IntentFilter filterAttached = new IntentFilter();
		filterAttached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		registerReceiver(mUsbReceiver, filterAttached);

		webview = (WebView) findViewById(R.id.webview);

		WebSettings settings = webview.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(false);
		settings.setJavaScriptEnabled(true);
		settings.setSupportMultipleWindows(false);

		settings.setLoadsImagesAutomatically(true);
		settings.setLightTouchEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setLoadWithOverviewMode(true);

		WebView webview = (WebView) findViewById(R.id.webview);
		WebView.enableSlowWholeDocumentDraw();

		String RESULTDATA = "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td style=\"font-size:20px;\"><pre><p style=font-size:18px;>FPS_ID      : 4779<br />D/H         : NA<br />District    : Shimla<br />--------------------------------------------------------------<br />Receipt No  : 1117000055<br />Date        : 07-11-2017<br />Time        : 08:47 PM<br />Card Holder : MANORMA<br />Members     : 4<br />RC Id       : HP-201410-1056666<br />Card Type   : APL<br />Auth.Type   : Ration Card Sales<br />--------------------------------------------------------------<br />Sl Items | Rate   | Qty |  Amt<br /><span style=font-size:14px;>             Per Kg/Ltr<br /></span>--------------------------------------------------------------<br />1) APL RI  10.00   0.001    0.01<br />         Month 11      Year 2017<br />--------------------------------------------------------------<br /> Total (₹)               00.01<br />--------------------------------------------------------------<br /></p>   आपका राशन आपका अधिकार<br />     Toll Free : 1967<br />      www.epds.co.in<br /></pre><br /><br /><br /><br /></td></tr></table>";
		if (!RESULTDATA.equals(null)) {
			Log.e("info", RESULTDATA);
			webview.loadData(RESULTDATA, "text/html", null);
		}


	}

	private Bitmap getBitmapOfWebView(final WebView webView) {
		Picture picture = webView.capturePicture();
		Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		picture.draw(canvas);
		return bitmap;
	}

	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				WorkService.delHandler(mHandler);
		//		mHandler = null;
			//	WorkService.clear();


				if(linearLayoutUSBDevices !=null){
					linearLayoutUSBDevices.removeAllViews();
				}

			}else if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
				WorkService.cb = new IOCallBack() {
					// WorkThread线程回调
					public void OnOpen() {
						// TODO Auto-generated method stub
						if(null != mHandler)
						{
						/*	Message msg = mHandler.obtainMessage(Global.MSG_IO_ONOPEN);
							mHandler.sendMessage(msg);*/
						}
					}

					public void OnClose() {
						// TODO Auto-generated method stub
						if(null != mHandler)
						{
							Message msg = mHandler.obtainMessage(Global.MSG_IO_ONCLOSE);
							mHandler.sendMessage(msg);
						}
					}

				};
			//	mHandler = new MHandler(ConnectUSBActivity.this);
				WorkService.addHandler(mHandler);

				if (null == WorkService.workThread) {
					Intent intent1 = new Intent(ConnectUSBActivity.this, WorkService.class);
					startService(intent1);
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
					probe();
				} else {
					finish();
				}
			}
		}
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUsbReceiver);
		WorkService.delHandler(mHandler);
		mHandler = null;
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {

		default:
			break;

		}

	}
	public String new_englishPrintHP() {
		StringBuilder textData = new StringBuilder();
		textData.append("<pre>");
		textData.append("<p style=font-size:18px;>");
		textData.append(fixedlenght("FPS_ID") + "" + "\n");
		textData.append(fixedlenght("D/H") + "" + "\n");
		textData.append(fixedlenght("District ") + "" + "\n");
		textData.append("--------------------------------------------------------------" + "\n");
		textData.append(fixedlenght("Receipt No") + "" + "\n");
		textData.append(fixedlenght("Date") + "" + "\n");
		textData.append(fixedlenght("Time") + "" + "\n");
		String cardHolderName="NA";

		textData.append(fixedlenght("Members") + "" + "\n");
		textData.append(fixedlenght("RC Id") + "" + "\n");
		textData.append(fixedlenght("Card Type") +"" + "\n");
		textData.append(fixedlenght("Auth.Type") + "" + "\n");


		textData.append("--------------------------------------------------------------" + "\n");
		textData.append("Sl " + "Items" + "| " + "Rate" + "   | " + "Qty" + " |  " + "Amt" + "\n");
		textData.append("<span style=font-size:14px;>");
		textData.append("             " + "Per Kg/Ltr" +"\n");
		textData.append("</span>");

		textData.append("--------------------------------------------------------------" + "\n");
		textData.append(fixedlenght("A----") + "" + "\n");
		textData.append(fixedlenght("B----") + "" + "\n");
		textData.append(fixedlenght("C----") + "" + "\n");

		textData.append("--------------------------------------------------------------\n");
		textData.append("--------------------------------------------------------------\n");
		textData.append(fixedlenght("D----") + "" + "\n");
		textData.append(fixedlenght("E----") + "" + "\n");
		textData.append(fixedlenght("F----") + "" + "\n");
		textData.append(fixedlenght("G----") + "" + "\n");
		textData.append(fixedlenght("H----") + "" + "\n");
		textData.append("   1आपका राशन आपका अधिकार" + "\n");
		textData.append("   2आपका राशन आपका अधिकार" + "\n");
		textData.append("   3आपका राशन आपका अधिकार" + "\n");
		textData.append("   4आपका राशन आपका अधिकार" + "\n");
		textData.append("   5आपका राशन आपका अधिकार" + "\n");

		textData.append("   6आपका राशन आपका अधिकार" + "\n");
		textData.append("</p>");

		textData.append("   आपका राशन आपका अधिकार" + "\n");
		textData.append("     Toll Free : 1967" + "\n");
		textData.append("      www.epds.co.in" + "\n");
		textData.append("</pre>");
		textData.append("\n");
		textData.append("\n");
		Log.e("Print", textData.toString());
		return textData.toString();
	}

	private String fixedlenght(String text) {

		text = text.trim();
		if (text.length() < 12) {

			while (text.length() < 12) {
				text = text + " ";
			}
			text = text.substring(0,12 - 1) + " : ";
		}
		Log.e("text", text);
		return text/*.replaceAll(" ", "&nbsp;")*/;
	}

	private String gethtmlcontent(String content, int font) {
		Log.e(TAG, "content in getHtml..." + content);
		Log.e(TAG, "font in getHtml..." + font);

		String fontsize = "18px";
		if (font != 0) {
			fontsize = Integer.toString(font) + "px";
		}
		String htmlContent = "";
		htmlContent = "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">";
		htmlContent = new StringBuilder(String.valueOf(htmlContent)).append("<tr><td style=\"font-size:" + fontsize + ";\">").append(content.replaceAll("\n", "<br />")).append("</td></tr>").toString();

		htmlContent = new StringBuilder(String.valueOf(htmlContent)).append("</table>").toString();
		return htmlContent;
	}

	private void bitmapPrint(final String content) {
		try {
			View promptsView = LayoutInflater.from(ConnectUSBActivity.this).inflate(R.layout.sending_print, null);
			final WebView w = (WebView) promptsView.findViewById(R.id.mywebView);
			android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(ConnectUSBActivity.this);
			alertDialogBuilder.setView(promptsView);
			alertDialogBuilder.setCancelable(true);
			alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			w.setWebViewClient(new WebViewClient() {
				public void onPageFinished(WebView view, String url) {
					Log.e(TAG,"w.getwidth "+w.getWidth()+"w.height "+w.getHeight());
				//	if (w.getWidth() <= 0 || w.getHeight() <= 0) {
						Log.e(TAG,"inside");
						Handler handler = new Handler();
						final WebView webView = w;
						final WebView webView2 = view;
						handler.postDelayed(new Runnable() {
							public void run() {
								if (webView.getWidth() > 0 && webView.getHeight() > 0) {
									final WebView webView1 = webView;
									final WebView webView22 = webView2;
									runOnUiThread(new Runnable() {
										public void run() {
											makeBitmap(webView1, webView22, content);
										}
									});
								}
							}
						}, (long) 500);
					//}


				}
			});
			w.setLayoutParams(new RelativeLayout.LayoutParams(Math.round((float) (48 * 8)), -2));
			w.getSettings().setAllowFileAccess(true);
			w.getSettings().setBuiltInZoomControls(true);
			w.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void makeBitmap(WebView w, WebView view, String content) {
		Log.e(TAG, "inside makeBitmap..." + content);
		try {
		float scale = getResources().getDisplayMetrics().density;
		int imgWidth = w.getWidth();
		int imgHeight = (int) (((float) view.getContentHeight()) * scale);
		Bitmap billbitmap = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(billbitmap);
		c.drawColor(-1);
		w.draw(c);

			Bitmap bitmap;
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
				 /*if(GlobalAppState.language.equals("hi")){
					 bitmap = drawableToBitmap(getDrawable(R.drawable.hindi_print_header));
				 }else{*/
			bitmap = drawableToBitmap(getDrawable(R.drawable.english_print_header));
			//}

			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			Bitmap btMap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
			if (btMap != null) {
				if (WorkService.workThread.isConnected()) {
					Bundle data = new Bundle();
					// data.putParcelable(Global.OBJECT1, mBitmap);
					data.putParcelable(Global.PARCE1, btMap);
					data.putInt(Global.INTPARA1, 384);
					data.putInt(Global.INTPARA2, 0);
					WorkService.workThread.handleCmd(
							Global.CMD_POS_PRINTBWPICTURE, data);
				} else {
					Toast.makeText(ConnectUSBActivity.this, Global.toast_notconnect,
							Toast.LENGTH_SHORT).show();
				}
				//pos.POS_PrintPicture(btMap, nPrintWidth, 1, nCompressMethod);
			}
			stream = new ByteArrayOutputStream();
			billbitmap = PrintImage(billbitmap);
			billbitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byteArray = stream.toByteArray();
			btMap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
			if (btMap != null) {
				if (WorkService.workThread.isConnected()) {
					Bundle data = new Bundle();
					// data.putParcelable(Global.OBJECT1, mBitmap);
					data.putParcelable(Global.PARCE1, btMap);
					data.putInt(Global.INTPARA1, 384);
					data.putInt(Global.INTPARA2, 0);
					WorkService.workThread.handleCmd(
							Global.CMD_POS_PRINTBWPICTURE, data);
				} else {
					Toast.makeText(ConnectUSBActivity.this, Global.toast_notconnect,
							Toast.LENGTH_SHORT).show();
				}
				// pos.POS_PrintPicture(btMap, nPrintWidth, 1, nCompressMethod);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		alertDialog.dismiss();
	}
	public  Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = null;

		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			bitmap = bitmapDrawable.getBitmap();
			if (bitmap != null) {
//                Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
				return bitmap;
			}
		}

		if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	public Bitmap PrintImage(Bitmap bm_source) {
		int width = bm_source.getWidth();
		int height = bm_source.getHeight();
		if (width > 576) {
			bm_source = getResizedBitmap(bm_source, 576, (int) Math.floor((double) (((float) height) * (((float) 576) / ((float) width)))));
		} else if (Math.floor((double) (width / 8)) != ((double) width) / 8.0d) {
			int newWidth = (int) Math.floor((double) (width / 8));
			bm_source = getResizedBitmap(bm_source, newWidth, (int) Math.floor((double) (((float) height) * (((float) newWidth) / ((float) width)))));
		}
		return bm_source;
	}
	public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / ((float) width);
		float scaleHeight = ((float) newHeight) / ((float) height);
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		bm.recycle();
		return resizedBitmap;
	}

	private void probe1() {
		final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		boolean isPrinterConnected =false;
		boolean isInterfaceFound = false;
		if (deviceList.size() > 0) {

			while (deviceIterator.hasNext()) {
				final UsbDevice device = deviceIterator.next();
				if(device.getVendorId() == 4070 && device.getInterfaceCount() ==1) {

					Log.e(TAG,"<==== Interface Count =====> "+device.getInterfaceCount());
					isPrinterConnected=true;
						PendingIntent mPermissionIntent = PendingIntent
								.getBroadcast(
										ConnectUSBActivity.this,
										0,
										new Intent(
												ConnectUSBActivity.this
														.getApplicationInfo().packageName),
										0);
						if (!mUsbManager.hasPermission(device)) {
							mUsbManager.requestPermission(device,
									mPermissionIntent);
							Toast.makeText(getApplicationContext(),
									Global.toast_usbpermit, Toast.LENGTH_LONG)
									.show();
						} else {
							WorkService.workThread.connectUsb(mUsbManager,
									device);
						}

				}
			}

			if(!isPrinterConnected){
				Toast.makeText(ConnectUSBActivity.this,"Printer Not found",Toast.LENGTH_SHORT).show();
			}
		}
	}



	private void probe() {
		final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		if (deviceList.size() > 0) {
			// 初始化选择对话框布局，并添加按钮和事件

			while (deviceIterator.hasNext()) { // 这里是if不是while，说明我只想支持一种device
				final UsbDevice device = deviceIterator.next();
				/*Toast.makeText(
						this,
						"" + device.getDeviceId() + device.getDeviceName()
								+ device.toString(), Toast.LENGTH_LONG).show();*/

				Button btDevice = new Button(
						linearLayoutUSBDevices.getContext());
				btDevice.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, 80));
				btDevice.setGravity(Gravity.CENTER_VERTICAL
						| Gravity.LEFT);
				btDevice.setText(String.format(" VID:%04X PID:%04X",
						device.getVendorId(), device.getProductId()));
				/*btnPrint.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PendingIntent mPermissionIntent = PendingIntent
								.getBroadcast(
										ConnectUSBActivity.this,
										0,
										new Intent(
												ConnectUSBActivity.this
														.getApplicationInfo().packageName),
										0);
						if (!mUsbManager.hasPermission(device)) {
							mUsbManager.requestPermission(device,
									mPermissionIntent);
							Toast.makeText(getApplicationContext(),
									Global.toast_usbpermit, Toast.LENGTH_LONG)
									.show();
						} else {
							WorkService.workThread.connectUsb(mUsbManager,
									device);
						}
					}
				});*/
				btDevice.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {

					//	Toast.makeText(ConnectUSBActivity.this,"<==== Interface Count && Device ====>"+device.getInterfaceCount()+" & "+device.getVendorId(),Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"<=== Button Clicked =======>");
							PendingIntent mPermissionIntent = PendingIntent
									.getBroadcast(
											ConnectUSBActivity.this,
											0,
											new Intent(
													ConnectUSBActivity.this
															.getApplicationInfo().packageName),
											0);
							if (!mUsbManager.hasPermission(device)) {
								mUsbManager.requestPermission(device,
										mPermissionIntent);
								Toast.makeText(getApplicationContext(),
										Global.toast_usbpermit, Toast.LENGTH_LONG)
										.show();
							} else {
								WorkService.workThread.connectUsb(mUsbManager,
										device);
							}
						}
				});
				if(device.getVendorId() == 4070 && device.getInterfaceCount() == 1) {
					linearLayoutUSBDevices.addView(btDevice);
				}
			}
		}
	}

	 class MHandler extends Handler {

		WeakReference<ConnectUSBActivity> mActivity;

		MHandler(ConnectUSBActivity activity) {
			mActivity = new WeakReference<ConnectUSBActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ConnectUSBActivity theActivity = mActivity.get();
			Log.e(TAG," msg What "+msg.what);
			Log.e(TAG," activity Reference "+mActivity.get());

			switch (msg.what) {

			case Global.MSG_WORKTHREAD_SEND_CONNECTUSBRESULT: {
				int result = msg.arg1;
				/*Toast.makeText(
						theActivity,
						(result == 1) ? Global.toast_success
								: Global.toast_fail, Toast.LENGTH_SHORT).show();*/
				Log.e(TAG, "Connect Result: " + result);
				if (1 == result) {
				//	PrintTest();
					String content="<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td style=\"font-size:20px;\"><pre><p style=font-size:18px;>FPS_ID      : 4779<br />D/H         : NA<br />District    : Shimla<br />--------------------------------------------------------------<br />Receipt No  : 1117000051<br />Date        : 07-11-2017<br />Time        : 08:30 PM<br />Card Holder : MANORMA<br />Members     : 4<br />RC Id       : HP-201410-1056666<br />Card Type   : APL<br />Auth.Type   : Ration Card Sales<br />--------------------------------------------------------------<br />Sl Items | Rate   | Qty |  Amt<br /><span style=font-size:14px;>             Per Kg/Ltr<br /></span>--------------------------------------------------------------<br />1) WHEAT   08.60   0.001    0.01<br />         Month 11      Year 2017<br />--------------------------------------------------------------<br /> Total (₹)               00.01<br />--------------------------------------------------------------<br /></p>   आपका राशन आपका अधिकार<br />     Toll Free : 1967<br />      www.epds.co.in<br /></pre><br /><br /><br /><br /></td></tr></table>";
			    // bitmapPrint(gethtmlcontent(new_englishPrintHP(), 20));
					bitmapPrint(content);
					//printBitmap();
				}
				break;
			}

				case Global.CMD_POS_PRINTPICTURERESULT: {
					int result = msg.arg1;
					Log.e(TAG, "bitmap Print Result: " + result);
					Toast.makeText(theActivity,
							(result == 1) ? " Success"
									: "Unable To print ", Toast.LENGTH_SHORT).show();

					break;
				}

			}
		}


		 private void printBitmap(){
			// Bitmap bm = getImageFromAssetsFile("yellowmen.png");
			 Bitmap bm = getBitmapOfWebView(webview);

			 if (bm != null) {
				 if (WorkService.workThread.isConnected()) {
					 Bundle data = new Bundle();
					 // data.putParcelable(Global.OBJECT1, mBitmap);
					 data.putParcelable(Global.PARCE1, bm);
					 data.putInt(Global.INTPARA1, 384);
					 data.putInt(Global.INTPARA2, 0);
					 WorkService.workThread.handleCmd(
							 Global.CMD_POS_PRINTBWPICTURE, data);
				 } else {
					 Toast.makeText(ConnectUSBActivity.this,"" ,
							 Toast.LENGTH_SHORT).show();
				 }
			 }
		 }
		 private Bitmap getImageFromAssetsFile(String fileName) {
			 Bitmap image = null;
			 AssetManager am = getResources().getAssets();
			 try {
				 InputStream is = am.open(fileName);
				 image = BitmapFactory.decodeStream(is);
				 is.close();
			 } catch (IOException e) {
				 e.printStackTrace();
			 }

			 return image;

		 }



		 void PrintTest() {
			String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n0123456789\n\n1\n2\n3\n4\n5\n6\n7\n8\n9\nEND";
			byte[] tmp1 = { 0x1b, 0x40, (byte) 0xB2, (byte) 0xE2, (byte) 0xCA,
					(byte) 0xD4, (byte) 0xD2, (byte) 0xB3, 0x0A };
			byte[] tmp2 = { 0x1b, 0x21, 0x01 };
			byte[] tmp3 = { 0x0A, 0x0A, 0x0A, 0x0A };
			byte[] buf = DataUtils.byteArraysToBytes(new byte[][] { tmp1,
					str.getBytes(), tmp2, str.getBytes(), tmp3 });
			if (WorkService.workThread.isConnected()) {
				Bundle data = new Bundle();
				data.putByteArray(Global.BYTESPARA1, buf);
				data.putInt(Global.INTPARA1, 0);
				data.putInt(Global.INTPARA2, buf.length);
				WorkService.workThread.handleCmd(Global.CMD_WRITE, data);
			} else {
				Toast.makeText(mActivity.get(), Global.toast_notconnect,
						Toast.LENGTH_SHORT).show();
			}
		}




	}

}