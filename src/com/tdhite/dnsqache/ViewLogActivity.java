/*
This file is part of DnsQache.

DnsQache is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

DnsQache is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DnsQache.  If not, see <http://www.gnu.org/licenses/>.

Copyright (c) 2012-2013 Tom Hite
Portions also Copyright (c) 2009 by Harald Mueller and Sofia Lemons.

*/

package com.tdhite.dnsqache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import com.tdhite.dnsqache.system.ConfigManager;

import android.os.Bundle;
import android.app.Activity;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ViewLogActivity extends Activity {
	static public final String TAG = "DNSMASQ -> ViewLogActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_log);

		Bundle b = getIntent().getExtras();
		int id = b.getInt(MainActivity.VIEW_LOG_FILE_EXTRA);

		WebView webView = (WebView) findViewById(R.id.webviewLog);
		webView.getSettings().setJavaScriptEnabled(false);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		webView.getSettings().setSupportMultipleWindows(false);
		webView.getSettings().setSupportZoom(false);
		setWebViewContent(webView, id);
	}

	private static final String HTML_HEADER = "<html><head><title>background-color</title> "
			+ "<style type=\"text/css\"> "
			+ "body { background-color:#181818; font-family:Arial; font-size:100%; color: #ffffff } "
			+ ".date { font-family:Arial; font-size:80%; font-weight:bold} "
			+ ".done { font-family:Arial; font-size:80%; color: #2ff425} "
			+ ".failed { font-family:Arial; font-size:80%; color: #ff3636} "
			+ ".skipped { font-family:Arial; font-size:80%; color: #6268e5} "
			+ "</style> " + "</head><body>";
	private static final String HTML_FOOTER = "</body></html>";

	private void setWebViewContent(WebView webView, int id) {
		QacheService svc = QacheService.getSingleton();
		ConfigManager config = ConfigManager.getConfigManager();
		String logPath = null;
		switch (id) {
			case R.id.action_view_polipo_log:
				logPath = config.getPolipoLogFile();
				break;
			case R.id.action_view_tinyproxy_log:
				logPath = config.getTinyProxyLogFile();
				break;
			case R.id.action_view_log:
			default:
				logPath = config.getLogFile();
				if (svc != null) {
					svc.generateLogFile();
				}
				break;
		}
		String logs = this.readLogfile(logPath);
		CharSequence target = "\n";
		CharSequence replace = "<br/>";
		webView.loadDataWithBaseURL("fake://fakeme",
				HTML_HEADER + logs.replace(target, replace) + HTML_FOOTER, "text/html",
				"UTF-8", "fake://fakeme");
	}

	private String readLogfile(String logFile) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		String data = "";
		ConfigManager config = ConfigManager.getConfigManager();
		try {
			File file = new File(logFile);
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, "utf-8");
			char[] buff = new char[(int) file.length()];
			isr.read(buff);
			data = new String(buff);
		} catch (FileNotFoundException e) {
			data = this.getString(R.string.log_activity_filenotfound) + ":<br/>\n"
					+ config.getLogFile();
		} catch (Exception e) {
			data = this.getString(R.string.log_activity_filenotfound) + ":<br/>\n"
					+ config.getLogFile() + "<br/>\nException:<br/>\n" + e.toString();
		} finally {
			try {
				if (isr != null)
					isr.close();
				if (fis != null)
					fis.close();
			} catch (Exception e) {
				// nothing
			}
		}
		return data;
	}
}
