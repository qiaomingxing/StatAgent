package com.sunday.statagent;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by Sunday on 2016/1/25. 版本号：V2.1
 */
public class StatAgent {

	public Context context;
	// 以下部分为每次必传
	public String uuid;// 登录设备id,0ADA9D5D-5436-4B14-9D12-E83D433CD340
	public String session_id;// 唯一标识一次启动的id,
	public String client_type;// 客户端类型,1:乐家H5 2.乐家APP 3.乐家微信
	public String mem_guid;// 用户ID(mem_guid),95D83596-0DE2-86A8-B411-93F17EA76ACA
	public String client_time;// 客户端时间戳(精准到毫秒),1418624508972
	public String terminal_os;// 操作系统,1.android 2.iPhone OS
	public String ver;// APP版本,1.1.0.5
	public String traffic_channel;// 下载渠道，各大应用市场
	public String ip;// 140.207.97.98
	public String network;// 联网方式
	public String gps;// 经纬度，经度,纬度 31.288920,121.442708

	// 以下部分为每次选传
	public String area_code;// 区域代码，所选区域
	public String track_type;// 当前行为类型，1 页面； 2 事件；3 推送
	public String page_id;// 当前页面ID，需要完整页面ID定义
	public String page_col;// 页面栏位，需要完整栏位ID定义
	public String col_position;// 栏位位置，栏位位置
	public String col_pos_content;// 搜索关键字、类目搜索ID、商品ID，搜索关键字、类目搜索ID、商品ID
	public String curl_req_url;// 当前页URL
	public String remarks;// 灵活字段，根据实际情况定义，必须JSON格式
	public String entry_method;// 进入方式 ，1.直接进入 2.返回 3.后台激活 4.推送 5.scheme
	public String http_referer;// 上一个界面的page_id

	// 以下部分暂时不用传
	public String abtest;// 不用传
	public String access_time;// 访问时间戳(精准到毫秒),服务器时间,不用传

	public static long enterTime;
	public static final String SP_NAME = "com.sunday.agent";

	// 全局初始化
	public static void init(Context context, String client_type, boolean isDebug) {
		LogConfig.isDebug = isDebug;
		new StatAgent(context, client_type);
	}

	// 存储mem_guid
	public static void initMemberId(Context context, String mem_guid) {
		SharedPreferences.Editor editor = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE).edit();
		editor.putString("mem_guid", mem_guid);
		LogUtil.i("init:" + mem_guid);
		editor.commit();
	}

	// 埋点，在需要埋点的地方引用此方法
	public static void initAction(Context context, String area_code,
			String track_type, String page_id, String page_col,
			String col_position, String col_pos_content, String entry_method,
			String curl_req_url, String http_referer) {
		new StatAgent(context, area_code, track_type, page_id, page_col,
				col_position, col_pos_content, entry_method, curl_req_url,
				http_referer);
	}

	// home注册监听，用于判断两次时间间隔
	public static void initWatcher(Context context) {
		try {
			HomeWatcher.initWatcher(context);
			enterTime = new Date().getTime();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	// 解除home监听
	public static void stopWatcher(Context context) {
		try {
			HomeWatcher.stopWatcher(context);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 全局构造方法
	 */
	public StatAgent(Context context, String clienttype) {

		this.context = context;
		this.uuid = Installation.getID(context).toUpperCase();// 1、生成唯一UUID
		this.session_id = setCookie(context);// 2、session_id
		this.client_type = getClient_type(context, clienttype);// 3、客户端类型，1为乐家H5，2为乐家APP，3为乐家微信
		this.mem_guid = getMem_guid(context);// 4、mem_guid
		this.client_time = getClient_time();// 5、当前时间
		this.terminal_os = getTerminal_os();// 6、获取操作系统,1为Android
		this.ver = getVer(context);// 7、获取版本号
		this.traffic_channel = getTraffic_channel(context);// 8、获取渠道名
		this.ip = getIp(context);// 9、获取IP
		this.network = getNetwork(context);// 10、获取网络类型
		this.gps = getGps(context);// 11、获取GPS
		this.page_id = "0";// 埋点初始化时传0

		String url_req;
		if (TextUtils.isEmpty(traffic_channel)) {
			LogUtil.i("traffic_channel is empty!");
			url_req = "http://tracker.rrslj.com/1.gif?udid=" + uuid
					+ "&session_id=" + session_id + "&client_type="
					+ client_type + "" + "&mem_guid=" + mem_guid
					+ "&client_time=" + client_time + "&terminal_os="
					+ terminal_os + "" + "&ver=" + ver + "&ip=" + ip
					+ "&network=" + network + "&gps=" + gps + "&page_id="
					+ page_id;
		} else {
			LogUtil.i("traffic_channel is not empty!");
			url_req = "http://tracker.rrslj.com/1.gif?udid=" + uuid
					+ "&session_id=" + session_id + "&client_type="
					+ client_type + "" + "&mem_guid=" + mem_guid
					+ "&client_time=" + client_time + "&terminal_os="
					+ terminal_os + "" + "&ver=" + ver + "&traffic_channel="
					+ traffic_channel + "&ip=" + ip + "&network=" + network
					+ "&gps=" + gps + "&page_id=" + page_id;
		}

		/***************************** 打印的log *****************************/
		String log = "udid：" + uuid + "，session_id：" + session_id
				+ "，client_type：" + client_type + "" + "，mem_guid：" + mem_guid
				+ "，client_time：" + client_time + "，terminal_os：" + terminal_os
				+ "" + "，ver：" + ver + "，traffic_channel：" + traffic_channel
				+ "" + "，ip：" + ip + "，network：" + network + "，gps：" + gps;

//		LogUtil.i(url_req);
		LogUtil.i(log);
		sendHttp(url_req);
	}

	/**
	 * 埋点构造方法 可以在事件中直接调用initAction(context,'','2','1','','','');函数 函数initAction
	 * (area_code,track_type,page_id,page_col,col_position,col_pos_content)说明
	 *
	 * @param context
	 *            上下文 必须有
	 * @param area_code
	 *            区域id 无:''
	 * @param track_type
	 *            行为类型1 页面； 2 事件；3 推送
	 * @param page_id
	 *            当前页面id 无:'' 见参数表excel
	 * @param page_col
	 *            页面栏位 无:'' 见参数表excel
	 * @param col_position
	 *            栏位位置 无:'' 见参数表excel
	 * @param col_pos_content
	 *            搜索关键词 无:''
	 * @param entry_method
	 *            进入方式 1.直接进入 2.返回 3.后台激活 4.推送 5.scheme
	 * @param curl_req_url
	 * 			     当前页URL
	 * @param http_referer
	 *            上一个页面的page_id
	 */
	public StatAgent(Context context, String area_code, String track_type,
			String page_id, String page_col, String col_position,
			String col_pos_content, String entry_method, String curl_req_url,
			String http_referer) {

		this.context = context;
		this.uuid = Installation.getID(context).toUpperCase();// 1、生成UUID
		this.session_id = getCookie(context);// 2、session_id/
		this.client_type = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE).getString("client_type", "");// 3、客户端类型
		this.mem_guid = getMem_guid(context);// 4、mem_guid
		this.client_time = getClient_time();// 5、当前时间
		this.terminal_os = getTerminal_os();// 6、获取操作系统
		this.ver = getVer(context);// 7、获取版本号
		this.traffic_channel = getTraffic_channel(context);// 8、获取渠道名
		this.ip = getIp(context);// 9、获取IP
		this.network = getNetwork(context);// 10、获取网络类型
		this.gps = getGps(context);// 11、获取GPS
		this.area_code = area_code;
		this.track_type = track_type;
		this.page_id = page_id;
		this.page_col = page_col;
		this.col_position = col_position;
		this.col_pos_content = col_pos_content;
		this.curl_req_url = curl_req_url;
		this.remarks = "";
		this.entry_method = entry_method;
		this.http_referer = http_referer;

		String url_req;
		if (TextUtils.isEmpty(traffic_channel)) {
			LogUtil.i("traffic_channel is empty!");
			url_req = "http://tracker.rrslj.com/1.gif?udid=" + uuid
					+ "&session_id=" + session_id + "&client_type="
					+ client_type + "" + "&mem_guid=" + mem_guid
					+ "&client_time=" + client_time + "&terminal_os="
					+ terminal_os + "" + "&ver=" + ver + "&ip=" + ip
					+ "&network=" + network + "&gps=" + gps + "&track_type="
					+ track_type + "&curl_req_url=" + curl_req_url
					+ "&area_code=" + area_code + "&page_id=" + page_id
					+ "&page_col=" + page_col + "&col_position=" + col_position
					+ "&col_pos_content=" + col_pos_content + "&curl_req_url="
					+ curl_req_url + "&entry_method=" + entry_method
					+ "&http_referer=" + http_referer;
		} else {
			LogUtil.i("traffic_channel is not empty!");
			url_req = "http://tracker.rrslj.com/1.gif?udid=" + uuid
					+ "&session_id=" + session_id + "&client_type="
					+ client_type + "" + "&mem_guid=" + mem_guid
					+ "&client_time=" + client_time + "&terminal_os="
					+ terminal_os + "" + "&ver=" + ver + "&traffic_channel="
					+ traffic_channel + "&ip=" + ip + "&network=" + network
					+ "&gps=" + gps + "&track_type=" + track_type
					+ "&curl_req_url=" + curl_req_url + "&area_code="
					+ area_code + "&page_id=" + page_id + "&page_col="
					+ page_col + "&col_position=" + col_position
					+ "&col_pos_content=" + col_pos_content + "&curl_req_url="
					+ curl_req_url + "&entry_method=" + entry_method
					+ "&http_referer=" + http_referer;
		}

		/***************************** 打印的log ***********************************/
		String mainLog = "udid：" + uuid + "，session_id：" + session_id
				+ "，client_type：" + client_type + "" + "，mem_guid：" + mem_guid
				+ "，client_time：" + client_time + "，terminal_os：" + terminal_os
				+ "" + "，ver：" + ver + "，traffic_channel：" + traffic_channel
				+ "" + "，ip：" + ip + "，network：" + network + "，gps：" + gps;

		String log = "mem_guid：" + mem_guid + "，track_type：" + track_type
				+ "，curl_req_url：" + curl_req_url + "，area_code：" + area_code
				+ "，page_id：" + page_id + "，page_col：" + page_col
				+ "，col_position：" + col_position + "，col_pos_content："
				+ col_pos_content + "，curl_req_url：" + curl_req_url
				+ "，entry_method：" + entry_method + "，http_referer："
				+ http_referer;

//		LogUtil.i(url_req);
		LogUtil.i(mainLog);
		LogUtil.i(log);
		sendHttp(url_req);
	}

	/**
	 * get方式提交到服务器
	 *
	 * @param url_req
	 */
	private void sendHttp(final String url_req) {

		LogUtil.i("http_start");
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {

					URL url = new URL(url_req);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);

					connection.setRequestProperty("Accept-Charset", "utf-8");
					connection.setRequestProperty("contentType", "utf-8");

					// connection.setRequestProperty("Content-Type",
					// "application/x-www-form-urlencoded;charset=utf-8");
					int responseCode = connection.getResponseCode();
					if (responseCode == 200) {
						InputStream in = connection.getInputStream();
						// 对获取到的输入流进行读取
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(in, "UTF-8"));

						StringBuilder response = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							response.append(line);
						}
						LogUtil.i("http_success:" + responseCode + "");
					} else {
						LogUtil.i("http_error:" + responseCode + "");
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (connection != null) {
						connection.disconnect();
						LogUtil.i("http_exit");
					}
				}
			}
		}).start();

	}

	/**
	 * 2、 生成session_id
	 */

	public String setCookie(Context context) {
		LogUtil.i("first set session_id");
		String time = String.valueOf(new Date().getTime());
		SharedPreferences.Editor editor = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE).edit();
		editor.putString("session_id", time);
		editor.commit();
		return time;
	}

	public String getCookie(Context context) {
		SharedPreferences pref = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		session_id = pref.getString("session_id", "");
		long exitTime = pref.getLong("exitTime", 0);

		if (0 == exitTime) {
			LogUtil.i("return session_id");
			return session_id;
		} else {
			// long enterTime = new Date().getTime();
			if (DateDistance(30, enterTime, exitTime)) {
				LogUtil.i("More than 30 seconds");
				SharedPreferences.Editor editor = pref.edit();
				editor.remove("exitTime");
				editor.commit();
				return setCookie(context);
			} else {
				LogUtil.i("Less than 30 seconds");
				SharedPreferences.Editor editor = pref.edit();
				editor.remove("exitTime");
				editor.commit();
				return session_id;
			}
		}
	}

	/**
	 * @param @return
	 * @return boolean
	 * @throws
	 * @Description: 比较两个时间是否大于30秒钟
	 * @date 2015年12月23日
	 */
	public boolean DateDistance(int timeLag, long oneDate, long twoDate) {
		long timeLong = oneDate - twoDate;
		timeLong = timeLong / 1000;
		if (timeLag > timeLong) {
			/* 小于30秒钟 */
			return false;
		} else {
			/* 大于30秒钟 */
			return true;
		}
	}

	/**
	 * 获取当前时间
	 *
	 * @return
	 */
	public String getTime() {
		long time = new Date().getTime();
		String timeStr = String.valueOf(time);
		return timeStr;
	}

	/**
	 * 获取时间间隔
	 *
	 * @param
	 * @return
	 */
	public boolean getGapTime() {
		SharedPreferences pref = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		String time = pref.getString("session_id", "");
		int gapTime = Integer.parseInt(String.valueOf(new Date().getTime()))
				- Integer.parseInt(time);
		if (gapTime > 30 * 1000) {
			return false;// 清空session_id
		} else {
			return true;// 不清空session_id
		}
	}

	/**
	 * 3、 获取客户端类型
	 *
	 * @param client_type
	 *            客户端类型
	 */
	public String getClient_type(Context context, String client_type) {

		SharedPreferences.Editor editor = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE).edit();
		if (client_type.equals("1")) {
			editor.putString("client_type", "1");
		} else if (client_type.equals("2")) {
			editor.putString("client_type", "2");
		} else if (client_type.equals("3")) {
			editor.putString("client_type", "3");
		} else {
			return "";
		}
		editor.commit();
		return client_type;
	}

	/**
	 * 4、 获取用户id,mem_guid
	 *
	 * @return
	 */
	public String getMem_guid(Context context) {
		SharedPreferences pref = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		String mem_guid = pref.getString("mem_guid", "");
		LogUtil.i("get:" + mem_guid);
		return mem_guid;
	}

	/**
	 * 5、 获取客户端时间戳
	 *
	 * @return
	 */
	public String getClient_time() {
		return String.valueOf(new Date().getTime());
	}

	/**
	 * 6、 操作系统
	 *
	 * @return
	 */
	public String getTerminal_os() {
		return "1";
	}

	/**
	 * 7、 获取版本
	 *
	 * @param context
	 * @return
	 */
	public String getVer(Context context) {
		PackageInfo pi = null;
		try {
			PackageManager pm = context.getPackageManager();
			pi = pm.getPackageInfo(context.getPackageName(),
					PackageManager.GET_CONFIGURATIONS);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf(pi.versionName);

	}

	/**
	 * 8、 获取渠道名
	 *
	 * @param context
	 * @return 如果没有获取成功，那么返回值为空
	 */
	public String getTraffic_channel(Context context) {

		if (context == null) {
			return "";
		}
		String channelName = "";
		try {
			PackageManager packageManager = context.getPackageManager();
			if (packageManager != null) {
				// 注意此处为ApplicationInfo 而不是
				// ActivityInfo,因为友盟设置的meta-data是在application标签中，而不是某activity标签中，所以用ApplicationInfo
				ApplicationInfo applicationInfo = packageManager
						.getApplicationInfo(context.getPackageName(),
								PackageManager.GET_META_DATA);
				if (applicationInfo != null) {
					if (applicationInfo.metaData != null) {
						channelName = applicationInfo.metaData.getString("");
					}
				}

			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return channelName;
	}

	/**
	 * 9、 获取IP地址
	 *
	 * @param context
	 * @return
	 */
	public String getIp(Context context) {
		String IPAddress = "";
		// 获取数据IP地址
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address) {
						IPAddress = inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 获取WIFI的IP地址
		if (TextUtils.isEmpty(IPAddress)) {
			// 获取WIFI服务
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			// 判断FIFI是否开启
			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
			}
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int i = wifiInfo.getIpAddress();
			IPAddress = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
					+ ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
		}
		return IPAddress;
	}

	/**
	 * 10、 获取网络类型
	 *
	 * @param context
	 * @return 2G,3G,4G,WiFi
	 */
	public String getNetwork(Context context) {
		String netType = "WiFi";
		try {
			netType = NetworkUtil.getCurrentNetworkType(context);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return netType;
	}

	public double latitude = 0.0;// 纬度
	public double longitude = 0.0;// 经度

	/**
	 * 11、 获取经纬度
	 *
	 * @param context
	 * @return
	 */
	public String getGps(Context context) {

		try {
			LocationManager locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			// GPS
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				LogUtil.i("gps location！");
				Location location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					longitude = location.getLongitude();// 经度
					latitude = location.getLatitude();// 纬度
				} else {
					LocationListener locationListener = new LocationListener() {

						// Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
						@Override
						public void onStatusChanged(String provider,
								int status, Bundle extras) {

						}

						// Provider被enable时触发此函数，比如GPS被打开
						@Override
						public void onProviderEnabled(String provider) {

						}

						// Provider被disable时触发此函数，比如GPS被关闭
						@Override
						public void onProviderDisabled(String provider) {

						}

						// 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
						@Override
						public void onLocationChanged(Location location) {
							if (location != null) {
								longitude = location.getLongitude(); // 经度
								latitude = location.getLatitude(); // 纬度
							}
						}
					};
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 1000, 0,
							locationListener);
					Location location1 = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location1 != null) {
						longitude = location1.getLongitude(); // 经度
						latitude = location1.getLatitude(); // 纬度
					}
				}
			} else {
				LogUtil.i("network location");
				LocationListener locationListener = new LocationListener() {

					// Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {

					}

					// Provider被enable时触发此函数，比如GPS被打开
					@Override
					public void onProviderEnabled(String provider) {

					}

					// Provider被disable时触发此函数，比如GPS被关闭
					@Override
					public void onProviderDisabled(String provider) {

					}

					// 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
					@Override
					public void onLocationChanged(Location location) {
						if (location != null) {
							longitude = location.getLongitude(); // 经度
							latitude = location.getLatitude(); // 纬度
						}
					}
				};
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 1000, 0,
						locationListener);
				Location location1 = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location1 != null) {
					longitude = location1.getLongitude(); // 经度
					latitude = location1.getLatitude(); // 纬度
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return String.valueOf(latitude) + "," + String.valueOf(longitude);
	}
}
