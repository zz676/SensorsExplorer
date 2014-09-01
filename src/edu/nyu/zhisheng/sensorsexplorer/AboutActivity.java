/*
 * Copyright © 2014 Zhisheng Zhou.
 */

package edu.nyu.zhisheng.sensorsexplorer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.nyu.zhisheng.sensorsexplorer.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		TextView aboutBuild = (TextView) findViewById(R.id.aboutBuild);

		InputStream buildInStream = getResources().openRawResource(R.raw.build);
		ByteArrayOutputStream buildOutStream = new ByteArrayOutputStream();

		int i;
		try {
			i = buildInStream.read();
			while (i != -1) {
				buildOutStream.write(i);
				i = buildInStream.read();
			}
			buildInStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TextView aboutText = (TextView) findViewById(R.id.aboutText);
		Linkify.addLinks(aboutText, Linkify.WEB_URLS);
	}

}
