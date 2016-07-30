package com.rubyhuntersky.angleedit.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author wehjin
 * @since 8/2/14.
 */
public class XmlDocumentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    ((XmlInputStreamSource) getActivity()).getXmlInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        assert rootView != null;
        rootView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        TreeView treeView = (TreeView) rootView.findViewById(R.id.treeView);
        treeView.setModel(newTreeViewModel(document));

        TextView addElementButton = (TextView) rootView.findViewById(R.id.button_add_element);
        addElementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                ((LinearLayout) rootView.findViewById(R.id.elements_panel)).addView(
                        View.inflate(getActivity(), R.layout.cell_element, null));
                        */
            }
        });
        return rootView;
    }

    private TreeView.TreeViewModel newTreeViewModel(Document document) {
        return newTreeViewModel(document.getDocumentElement());
    }

    private TreeView.TreeViewModel newTreeViewModel(final Element element) {

        final List<TreeView.TreeViewModel> models = new ArrayList<>();

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                models.add(newTreeViewModel((Element) item));
            }
        }
        return new TreeView.TreeViewModel() {
            @Override
            public List<TreeView.TreeViewModel> getModels() {
                return models;
            }

            @Override
            public View newViewInstance() {
                final ElementCellViewHolder viewHolder = new ElementCellViewHolder(getActivity());
                viewHolder.bind(element);
                return viewHolder.getItemView();
            }
        };
    }

    public interface XmlInputStreamSource {
        InputStream getXmlInputStream() throws IOException;
    }
}
