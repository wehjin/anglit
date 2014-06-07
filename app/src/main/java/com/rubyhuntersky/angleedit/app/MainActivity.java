package com.rubyhuntersky.angleedit.app;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private LinearLayout elementsPanel;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            TreeView treeView = (TreeView) rootView.findViewById(R.id.treeView);

            Document document;
            try {
                InputStream open = getActivity().getResources().getAssets().open("sample.xml");
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document parse = documentBuilder.parse(open);
                document = parse;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            TreeView.TreeViewModel treeViewModel = newTreeViewModel(document);
            treeView.setModel(treeViewModel);

            elementsPanel = (LinearLayout) rootView.findViewById(R.id.elements_panel);

            TextView addElementButton = (TextView) rootView.findViewById(R.id.button_add_element);
            addElementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View elementView = View.inflate(getActivity(), R.layout.cell_element, null);
                    //elementsPanel.addView(elementView);
                }
            });
            return rootView;
        }

        private TreeView.TreeViewModel newTreeViewModel(Document document) {
            Element documentElement = document.getDocumentElement();
            return newTreeViewModel(documentElement);
        }

        private TreeView.TreeViewModel newTreeViewModel(final Element element) {

            final List<TreeView.TreeViewModel> models = new ArrayList<TreeView.TreeViewModel>();
            NodeList childNodes = element.getChildNodes();
            int length = childNodes.getLength();
            for (int i = 0; i < length; i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) item;
                    models.add(newTreeViewModel(child));
                }
            }
            return new TreeView.TreeViewModel() {
                @Override
                public List<TreeView.TreeViewModel> getModels() {
                    return models;
                }

                @Override
                public View newViewInstance() {
                    View inflate = View.inflate(getActivity(), R.layout.cell_element, null);
                    TextView textView = (TextView)inflate.findViewById(R.id.textView);
                    textView.setText(element.getTagName());
                    return inflate;
                }
            };
        }
    }
}
