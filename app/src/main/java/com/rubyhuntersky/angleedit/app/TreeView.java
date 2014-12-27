package com.rubyhuntersky.angleedit.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * @author wehjin
 * @since 2/23/14.
 */

/**
 * View displays a tree.
 */
public class TreeView extends ScrollView {

    public interface TreeViewModel {
        List<TreeViewModel> getModels();

        View newViewInstance();
    }

    private SlidePanel slidePanel;
    private final List<RowModel> rowModels = new ArrayList<>();
    private final PublishSubject<Integer> scrollTop = PublishSubject.create();

    @SuppressWarnings("UnusedDeclaration")
    public TreeView(Context context) {
        super(context);
        initTreeView();
    }

    @SuppressWarnings("UnusedDeclaration")
    public TreeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTreeView();
    }

    @SuppressWarnings("UnusedDeclaration")
    public TreeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTreeView();
    }

    private void initTreeView() {
        slidePanel = new SlidePanel(getContext());
        addView(slidePanel);

        scrollTop.throttleWithTimeout(5, TimeUnit.MILLISECONDS).distinctUntilChanged().observeOn(
                AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
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

    public void setModel(TreeViewModel treeViewModel) {
        List<RowModel> newRowModels = new ArrayList<>();
        appendRowModels(newRowModels, treeViewModel, 0);
        setRowModels(newRowModels);
        requestLayout();
    }

    private void appendRowModels(List<RowModel> rowModels, TreeViewModel cellModel, int depth) {
        if (cellModel == null) {
            return;
        }
        RowModel rowModel = new RowModel(depth, cellModel.newViewInstance());
        rowModels.add(rowModel);
        List<TreeViewModel> children = cellModel.getModels();
        for (TreeViewModel child : children) {
            appendRowModels(rowModels, child, depth + 1);
        }
    }

    private void setRowModels(List<RowModel> rowModels) {
        this.rowModels.clear();
        this.rowModels.addAll(rowModels);
        slidePanel.setupViews(this.rowModels);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        scrollTop.onNext(t);
    }

    static class RowModel {
        int depth = 0;
        final View view;

        public RowModel(int depth, View view) {
            this.depth = depth;
            this.view = view;
        }
    }

    class SlidePanel extends ViewGroup {

        final List<View> views = new ArrayList<>();
        private int heightPixels;
        private int indentPixels;
        private View selectedView;

        @SuppressLint("UseSparseArrays")
        private final Map<Integer, Integer> lowerRowTopAtDepth = new HashMap<>();

        SlidePanel(Context context) {
            super(context);
            initSlidePanel(context);
        }

        @SuppressWarnings("UnusedDeclaration")
        SlidePanel(Context context, AttributeSet attrs) {
            super(context, attrs);
            initSlidePanel(context);
        }

        @SuppressWarnings("UnusedDeclaration")
        SlidePanel(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initSlidePanel(context);
        }

        void initSlidePanel(Context context) {
            heightPixels = dipToPixels(context, 44);
            indentPixels = dipToPixels(context, 33);
        }

        public void setupViews(List<RowModel> flatModels) {
            removeAllViews();
            views.clear();
            for (RowModel flatModel : flatModels) {
                View view = flatModel.view;
                view.setTag(flatModel);
                addView(view, 0);
                views.add(view);

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("TreeView", "Clicked");
                        if (selectedView != null) {
                            selectedView.setSelected(false);
                        }
                        view.setSelected(true);
                        selectedView = view;
                    }
                });
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
            lowerRowTopAtDepth.clear();
            for (int index = views.size() - 1; index >= 0; index--) {
                View view = views.get(index);
                RowModel rowModel = (RowModel) view.getTag();

                int viewWidth = width - indentPixels * rowModel.depth;

                int widthMeasureSpec = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY);
                int heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightPixels,
                                                                    MeasureSpec.EXACTLY);
                view.measure(widthMeasureSpec, heightMeasureSpec);
                view.layout(width - viewWidth, 0, width, heightPixels);

                int viewNormalBottom = (index + 1) * heightPixels;

                int stickyYBottom = scrollY + heightPixels * (rowModel.depth + 1);
                int viewBottomBeforeLowerRowPressure = Math.max(viewNormalBottom, stickyYBottom);
                int viewBottomWithLowerRowPressure = getViewBottomWithLowerRowPressure(
                        viewBottomBeforeLowerRowPressure, rowModel.depth);
                int offsetFromStickyY = stickyYBottom - viewBottomWithLowerRowPressure;
                view.setAlpha(getRowAlpha(offsetFromStickyY));

                int viewTopAfterLowerRowPressure = viewBottomWithLowerRowPressure - heightPixels;
                view.setTranslationY(viewTopAfterLowerRowPressure);
                lowerRowTopAtDepth.put(rowModel.depth, viewTopAfterLowerRowPressure);
            }
        }

        private int getViewBottomWithLowerRowPressure(int viewBottomBeforeLowerRowPressure,
                                                      int depth) {
            Integer lowerRowTop = getHighestLowerRowTopForDepth(depth);
            return lowerRowTop == null ? viewBottomBeforeLowerRowPressure : Math.min(
                    viewBottomBeforeLowerRowPressure, lowerRowTop - 1);
        }

        private float getRowAlpha(int offsetFromStickyY) {
            final float underAlpha = .1f;
            if (offsetFromStickyY > heightPixels) {
                return underAlpha;
            } else if (offsetFromStickyY > 0) {
                return underAlpha + (1f - underAlpha) * (1.0f - offsetFromStickyY / (float) heightPixels);
            } else {
                return 1f;
            }
        }

        private Integer getHighestLowerRowTopForDepth(int depth) {
            Integer lowerRowTop = null;
            for (int i = 0; i <= depth; i++) {
                Integer lowerRowTopAtDepth = this.lowerRowTopAtDepth.get(i);
                if (lowerRowTopAtDepth != null) {
                    if (lowerRowTop == null) {
                        lowerRowTop = lowerRowTopAtDepth;
                    } else {
                        lowerRowTop = Math.min(lowerRowTop, lowerRowTopAtDepth);
                    }
                }
            }
            return lowerRowTop;
        }

    }

    private static int dipToPixels(Context context, float dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                                               context.getResources().getDisplayMetrics());
    }
}
