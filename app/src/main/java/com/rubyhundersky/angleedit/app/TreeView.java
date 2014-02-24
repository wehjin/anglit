package com.rubyhundersky.angleedit.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by wehjin on 2/23/14.
 */
public class TreeView extends ScrollView {

    private SlidePanel slidePanel;
    private List<CellModel> models = new ArrayList<CellModel>();
    private Timer timer;
    PublishSubject<Integer> scrollSubject = PublishSubject.create();

    public TreeView(Context context) {
        super(context);
        initTreeView();
    }

    public TreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTreeView();
    }

    public TreeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTreeView();
    }

    private void initTreeView() {
        slidePanel = new SlidePanel(getContext());
        addView(slidePanel);

        List<CellModel> models = new ArrayList<CellModel>();
        models.add(new CellModel(0, "Document"));
        models.add(new CellModel(1, "Section1"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        models.add(new CellModel(1, "Section2"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        models.add(new CellModel(1, "Section3"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        models.add(new CellModel(1, "Section4"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        models.add(new CellModel(1, "Section5"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        models.add(new CellModel(1, "Section4"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        models.add(new CellModel(1, "Section5"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        models.add(new CellModel(1, "Section6"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        models.add(new CellModel(1, "Section7"));
        models.add(new CellModel(2, "Paragraph 1"));
        models.add(new CellModel(2, "Paragraph 2"));
        models.add(new CellModel(2, "Paragraph 3"));
        setModels(models);

        scrollSubject.throttleWithTimeout(5, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("TreeView", e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(Integer args) {
                        slidePanel.requestLayout();
                    }
                });
    }

    void setModels(List<CellModel> models) {
        this.models.clear();
        this.models.addAll(models);
        slidePanel.setupViews(models);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        scrollSubject.onNext(t);
    }

    static class CellModel {
        int depth = 0;
        String text;

        public CellModel(int depth, String text) {
            this.depth = depth;
            this.text = text;
        }
    }

    class SlidePanel extends ViewGroup {

        List<TextView> views = new ArrayList<TextView>();
        private int heightPixels;
        private int indentPixels;

        SlidePanel(Context context) {
            super(context);
            initSlidePanel();
        }

        SlidePanel(Context context, AttributeSet attrs) {
            super(context, attrs);
            initSlidePanel();
        }

        SlidePanel(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initSlidePanel();
        }

        void initSlidePanel() {
            heightPixels = dipToPixels(getContext(), 44);
            indentPixels = dipToPixels(getContext(), 33);
        }

        public int dipToPixels(Context context, float dipValue) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
        }

        public void setupViews(List<CellModel> models) {
            removeAllViews();
            views.clear();
            for (CellModel model : models) {
                TextView textView = new TextView(getContext());
                textView.setText(model.text);
                textView.setTag(model);
                addView(textView, 0);
                views.add(textView);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(width, views.size() * heightPixels);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int width = getWidth();
            int scrollY = TreeView.this.getScrollY();
            Map<Integer, Integer> previousTopAtDepth = new HashMap<Integer, Integer>();
            for (int index = views.size() - 1; index >= 0; index--) {
                TextView view = views.get(index);
                CellModel cellModel = (CellModel) view.getTag();

                int viewWidth = width - indentPixels * cellModel.depth;
                view.layout(width - viewWidth, 0, width, heightPixels);

                int viewNormalBottom = (index + 1) * heightPixels;

                int stickyY = scrollY + heightPixels * (cellModel.depth + 1);
                int viewBottom = Math.max(viewNormalBottom, stickyY);

                Integer previousTop = null;
                for (int i = 0; i <= cellModel.depth; i++) {
                    Integer aPreviousTop = previousTopAtDepth.get(i);
                    if (previousTopAtDepth != null) {
                        if (previousTop == null) {
                            previousTop = aPreviousTop;
                        } else {
                            previousTop = Math.min(previousTop.intValue(), aPreviousTop.intValue());
                        }
                    }
                }
                if (previousTop != null) {
                    int viewBottomBeforePreviousTop = viewBottom;
                    viewBottom = Math.min(viewBottom, previousTop.intValue() - 1);
                }


                int offsetFromStickyY = stickyY - viewBottom;
                final float underAlpha = .1f;
                if (offsetFromStickyY > heightPixels) {
                    view.setAlpha(underAlpha);
                } else if (offsetFromStickyY > 0) {
                    view.setAlpha(underAlpha + (1f - underAlpha) * (1.0f - offsetFromStickyY / (float) heightPixels));
                } else {
                    view.setAlpha(1);
                }

                int viewTop = viewBottom - heightPixels;
                view.setTranslationY(viewTop);
                previousTopAtDepth.put(cellModel.depth, viewTop);
            }
        }

    }
}
