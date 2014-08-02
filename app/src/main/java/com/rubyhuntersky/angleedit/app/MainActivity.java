package com.rubyhuntersky.angleedit.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends ActionBarActivity implements XmlDocumentFragment.XmlInputStreamSource {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            loadFragment();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(MainActivity.class.getSimpleName(), "New intent: " + intent);
        setIntent(intent);
        loadFragment();
    }

    @Override
    public InputStream getXmlInputStream() throws IOException {
        Uri data = getIntent().getData();
        Log.d(MainActivity.class.getSimpleName(), "Data uri: " + data);
        return (data == null) ? getSampleInputStream() : new FileInputStream(
                new File(data.getPath()));
    }

    private void loadFragment() {
        XmlDocumentFragment xmlDocumentFragment = new XmlDocumentFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                                                               xmlDocumentFragment).commit();
    }

    private InputStream getSampleInputStream() throws IOException {
        return getResources().getAssets().open("sample.xml");
    }

}
