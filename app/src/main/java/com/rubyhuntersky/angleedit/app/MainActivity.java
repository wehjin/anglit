package com.rubyhuntersky.angleedit.app;

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
            XmlDocumentFragment xmlDocumentFragment = new XmlDocumentFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container,
                                                               xmlDocumentFragment).commit();
        }
    }

    @Override
    public InputStream getXmlInputStream() throws IOException {
        Uri data = getIntent().getData();
        Log.d(MainActivity.class.getSimpleName(), "Data uri: " + data);
        return (data == null) ? getSampleInputStream() : new FileInputStream(
                new File(data.getPath()));
    }

    private InputStream getSampleInputStream() throws IOException {
        return getResources().getAssets().open("sample.xml");
    }

}
