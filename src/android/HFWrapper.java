package com.heytz.cordova;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;
import com.hiflying.smartlink.OnSmartLinkListener;
import com.hiflying.smartlink.SmartLinkedModule;
import com.hiflying.smartlink.v3.SnifferSmartLinker;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.JSONObjectBody;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;


public class HFWrapper extends CordovaPlugin implements OnSmartLinkListener {

    private static final String TAG = "==HFWrapper==";
    // todo :  update to domain name
    public static final String AU_URI = "http://m2m.heytz.com/device/authorize";

    private CallbackContext airLinkCallbackContext;
    private Context context;
    private String appId;
    private SnifferSmartLinker snifferSmartLinker;
    private boolean mIsConnecting = false;
    private String productKey;
    private String userToken;
    private String uid;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // your init code here
        context = cordova.getActivity().getApplicationContext();
        // init SmartLinker
        snifferSmartLinker = SnifferSmartLinker.getInstence();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {

            // [appId, productKey, uid, userToken, wifiSSID, wifiKey]
            appId = args.getString(0);
            productKey = args.getString(1);
            uid = args.getString(2);
            userToken = args.getString(3);
            String wifiSSid = args.getString(4);
            String wifiKey = args.getString(5);

            Log.d(TAG, "appId:" + appId);
            Log.d(TAG, "productKey:" + productKey);
            Log.d(TAG, "uid:" + uid);
            Log.d(TAG, "userToken:" + userToken);
            Log.d(TAG, "wifiSSid:" + wifiSSid);
            Log.d(TAG, "wifiKey:" + wifiKey);

            airLinkCallbackContext
                    = callbackContext;
            this.start(wifiSSid, wifiKey);

            return true;
        }

        if (action.equals("dealloc")) {
            this.dealloc();
            return true;
        }
        return false;
    }

    private void start(String wifiSSID, String wifiKey) {
        if (!mIsConnecting) {
            //设置要配置的ssid 和 pwd
            try {
                snifferSmartLinker.setOnSmartLinkListener(this);
                //开始 smartLink
                snifferSmartLinker.start(context, wifiKey, wifiSSID);
                mIsConnecting = true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }
    }

    private void dealloc() {
        snifferSmartLinker.setOnSmartLinkListener(null);
        snifferSmartLinker.stop();
        mIsConnecting = false;
    }

    /**
     * To send the command of get-device-id to the specified module
     *
     * @param moduleInfo
     * @param appInfo
     * @param userInfo
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void sendGetDidCommand(final ModuleInfo moduleInfo, final AppInfo appInfo, final UserInfo userInfo)
            throws AssertionError, InterruptedException {
        if ((moduleInfo.getModuleIP() == null) || moduleInfo.getModuleIP().isEmpty()) {
            throw new AssertionError();
        }
        new Thread() {
            Socket socket = null;

            @Override
            public void run() {
                try {
                    Log.i(TAG, moduleInfo.getModuleIP());
                    boolean isConnected = false;
                    try {
                        while (!isConnected) {
                            socket = new Socket(moduleInfo.getModuleIP(), moduleInfo.getPort());
                            isConnected = true;
                        }
                    } catch (SocketException se) {
                        Log.e(TAG, se.toString());
                    }
                    //socket = new Socket("192.168.10.63", port);
                    if (socket.isConnected()) {
                        final OutputStream os = socket.getOutputStream();
                        String cmd = "{" + "\"app_id\":\"" + appInfo.getApp_id() + "\"," +
                                "\"product_key\":\"" + appInfo.getProduct_key() + "\"," +
                                "\"user_token\":\"" + userInfo.getUser_token() + "\"," +
                                "\"uid\":\"" + userInfo.getUid() +
                                //20151030汉枫周工建议去除该参数，因为建立连接时模块能获取到手机的ip "," + "\"client_ip\":" + client_ip +
                                "\"}";
                        os.write(cmd.getBytes());
                        Log.i(TAG, cmd);

                        InputStream is = socket.getInputStream();
                        byte[] reply = new byte[0];
                        try {
                            reply = HFWrapper.readStream(is);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                        }
                        final String replyMessages = new String(reply);
                        Log.i(TAG, "get the did socket message: " + replyMessages);
                        JSONObject authorizeCommand = null;
                        try {
                            authorizeCommand = new JSONObject(cmd);
                            authorizeCommand.put("did", replyMessages);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //TODO get the did sent from module, send to server do authentication again
                        JSONObjectBody writer;
                        try {
                            writer = new JSONObjectBody(authorizeCommand);

                            AsyncHttpPost post = new AsyncHttpPost(AU_URI);
                            post.setBody(writer);
                            final byte[] finalReply = reply;
                            AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
                                        @Override
                                        public void onCompleted(Exception e, AsyncHttpResponse asyncHttpResponse, JSONObject jsonObject) {
                                            if (e != null) {
                                                e.printStackTrace();
                                                return;
                                            }
                                            try {
                                                Log.d(TAG, asyncHttpResponse.message());
                                                int result = jsonObject.getInt("affected");
                                                Log.d(TAG, "" + result);

                                                Log.i(TAG, "Socket closed.");
                                                if (result == 1) {
                                                    //after authentication succeeds, send did back to module to complete the whole process,
                                                    os.write(finalReply);
                                                    Log.i(TAG, "did sent to module.");

                                                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, replyMessages);
                                                    airLinkCallbackContext.sendPluginResult(pluginResult);
                                                } else {
                                                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
                                                    airLinkCallbackContext.sendPluginResult(pluginResult);
                                                }

                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                            );
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                } finally {
                    if (socket != null)
                        try {
                            socket.close();
                            Log.i(TAG, "Socket closed.");
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                            e.printStackTrace();
                        }
                }
            }
        }.start();
    }

    /**
     * @param inStream
     * @return 字节数组
     * @throws Exception
     * @功能 读取流
     */
    private static byte[] readStream(InputStream inStream) throws Exception {
        int count = 0;
        while (count == 0) {
            count = inStream.available();
            Log.i(TAG, String.valueOf(count));
        }
        byte[] b = new byte[count];
        inStream.read(b);
        return b;
    }


    /**
     * get mobile device ip address as a string IPV4 only
     *
     * @return current mobile device ip address
     */
    private String getMobileIpAddress() {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    @Override
    public void onLinked(SmartLinkedModule smartLinkedModule) {
        try {
            ModuleInfo moduleInfo = new ModuleInfo(smartLinkedModule.getModuleIP(), 8000);
            AppInfo appInfo = new AppInfo(appId, productKey);
            UserInfo userInfo = new UserInfo(userToken, uid);
            sendGetDidCommand(moduleInfo, appInfo, userInfo);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onTimeOut() {

    }
}