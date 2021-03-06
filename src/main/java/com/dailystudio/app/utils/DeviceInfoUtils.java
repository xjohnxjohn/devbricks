package com.dailystudio.app.utils;

import com.dailystudio.development.Logger;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DeviceInfoUtils {

	public static boolean isRoaming(Context context) {
        if (context == null) {
            return false;
        }

        TelephonyManager telmgr =
                (TelephonyManager) context.getSystemService(
                        Context.TELEPHONY_SERVICE);

        return telmgr.isNetworkRoaming();
    }

    public static boolean isMobileNetwork(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connmgr =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = connmgr.getActiveNetworkInfo();

        return (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static String getIMSI(Context context) {
        if (context == null) {
            return null;
        }
        
        TelephonyManager telmgr = 
                (TelephonyManager) context.getSystemService(
                        Context.TELEPHONY_SERVICE);
        
        String imsi = telmgr.getSubscriberId();

        return imsi;
    }

    public static String getModel(Context context) {
        return Build.MODEL;
    }

    public static String getMacAddress(Context context) {
        if (context == null) {
            return null;
        }

        WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return null;
        }

        WifiInfo wInfo = wifiManager.getConnectionInfo();
        if (wInfo == null) {
            return null;
        }

        return wInfo.getMacAddress();
    }
    
    public static String getAndroidId(Context context) {
    	if (context == null) {
    		return null;
    	}
    	
    	final ContentResolver cr = context.getContentResolver();
    	if (cr == null) {
    		return null;
    	}
    	
    	return Secure.getString(cr, Secure.ANDROID_ID);
    }

	private static final Uri URI = Uri
			.parse("content://com.google.android.gsf.gservices");

	private static final String ID_KEY = "android_id";

	public static String getGSFAndroidId(Context context) {
		String[] params = { ID_KEY };
		Cursor c = context.getContentResolver().query(URI, null, null, params,
				null);
		if (c == null) {
			return null;
		}

		if (!c.moveToFirst() || c.getColumnCount() < 2) {
			c.close();

			return null;
		}

		String aId = null;
		try {
			aId = Long.toHexString(Long.parseLong(c.getString(1)));
		} catch (NumberFormatException e) {
			Logger.warn("parse aid failure: %s", e.toString());

			aId = null;
		}

		c.close();

		return aId;
	}

    public static String getPhoneNumber(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager telmgr =
                (TelephonyManager) context.getSystemService(
                        Context.TELEPHONY_SERVICE);

        return telmgr.getLine1Number();
    }

    public static String getRegisteredNetwork(Context context) {
        if (context == null) {
            return null;
        }

        TelephonyManager telmgr =
                (TelephonyManager) context.getSystemService(
                        Context.TELEPHONY_SERVICE);
        if (telmgr == null) {
            return null;
        }

        String countryCode = null;
    	/*
    	 * XXX: result of getNetworkCountryIso() is unreliable on CDMA
    	 * 		network or user is not register to a valid network.
    	 * 		During this situation, we use MCC for fallback;
    	 */
        if (telmgr.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
            countryCode = telmgr.getNetworkCountryIso();
        } else {
            countryCode = telmgr.getSimCountryIso();
        }

        return countryCode;
    }

    public static String getSimCountryIso(Context context) {
        TelephonyManager telmgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telmgr == null) {
            return null;
        }

        return telmgr.getSimCountryIso();
    }

    public static String getIPAddress(boolean useIPv4) {
        return getIPAddress(useIPv4, false);
    }

    public static String getIPAddress(boolean useIPv4, boolean containsP2p) {
        String ipAddr = "";

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface netIntf : interfaces) {
                if (netIntf.getName().contains("p2p") && !containsP2p) {
                    continue;
                }

                List<InetAddress> netAddrs = Collections.list(netIntf.getInetAddresses());
                for (InetAddress addr : netAddrs) {
                    if (!addr.isLoopbackAddress()) {
                        String strAddr = addr.getHostAddress();
                        Logger.debug("strAddr = %s", strAddr);
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(strAddr);
                        boolean isIPv4 = strAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4) {
                                ipAddr = strAddr;
                            }
                        } else {
                            if (!isIPv4) {
                                int delim = strAddr.indexOf('%'); // drop ip6 zone suffix
                                ipAddr = (delim < 0 ?
                                        strAddr.toUpperCase()
                                        : strAddr.substring(0, delim).toUpperCase());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.warn("could not get local ip: %s", ex.toString());
        }

        return ipAddr;
    }

}
