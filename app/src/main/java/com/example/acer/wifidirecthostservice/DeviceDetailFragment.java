package com.example.acer.wifidirecthostservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import com.example.acer.wifidirecthostservice.DeviceListFragment.DeviceActionListener;

/*
  A fragment that manages a particular peer and allows interaction with device
  i.e. setting up network connection and transferring data.
*/
public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private static boolean server_running = false;

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    public WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    String filetype;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                Log.d("Wifi Connect", device.deviceAddress);
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
                        //                        new DialogInterface.OnCancelListener() {
                        //
                        //                            @Override
                        //                            public void onCancel(DialogInterface dialog) {
                        //                                ((DeviceActionListener) getActivity()).cancelDisconnect();
                        //                            }
                        //                        }
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

		/*mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Allow user to pick an image from Gallery or other
						// registered apps
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("image/*");
						filetype = "image";
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
					}
				});*/
/*              mContentView.findViewById(R.id.Remote).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //Starts up the touch screen
                        try
                        {
                            Class screen = Class.forName("com.example.android.wifidirect.RemoteScreen");
                            Intent intent = new Intent(getActivity(),screen);
                            Log.d("DeviceDetail", "Before starting the intent");
                            intent.putExtra("DeviceAddress", device.deviceAddress);
                            Log.d("Remote Screen Call", device.deviceAddress);
                            intent.putExtra("IP_SERVER", IP_SERVER);
                            intent.putExtra("PORT", PORT);
                            startActivity(intent);
                        }catch(ClassNotFoundException e)
                        {
                            Toast.makeText(getActivity(), "Class not Found", Toast.LENGTH_SHORT).show();
                        }

                    }

                });
*/
        return mContentView;
    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        String localIP = Utils.getLocalIPAddress();
        // Trick to find the ip in the file /proc/net/arp
        String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
        String clientIP = Utils.getIPFromMac(client_mac_fixed);

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Bundle b = new Bundle();
        b = data.getExtras();
        byte bytedata[] = b.getByteArray("TOUCH_POS");

        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: ");
        Log.d(WiFiDirectActivity.TAG, "Intent----------- ");
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_NUMBER_STREAM, bytedata);
        //serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());

        if(localIP.equals(IP_SERVER)){
            serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIP);
        }else{
            serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, IP_SERVER);
        }

        serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, PORT);
        getActivity().startService(serviceIntent);
    }
*/
    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Log.d("OnConnection", "Callback method entered");
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        //mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
        mContentView.findViewById(R.id.Remote).setVisibility(View.VISIBLE);
        Log.d("OnConnection", "Before starting service");
        //Intent serviceIntent = new Intent(getActivity(), HostService.class);
        //getActivity().startService(serviceIntent);
        //if (!server_running){
           // new ServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
           // server_running = true;
        //}

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);

    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        //mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        mContentView.findViewById(R.id.Remote).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }





}
