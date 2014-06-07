package com.rubyhuntersky.angleedit.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
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
    private List<FlatCellModel> flatModels = new ArrayList<FlatCellModel>();
    private Timer timer;
    PublishSubject<Integer> scrollTop = PublishSubject.create();

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

        scrollTop.throttleWithTimeout(5, TimeUnit.MILLISECONDS)
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

    public void setModel(TreeViewModel treeViewModel) {
        List<FlatCellModel> flatModels = new ArrayList<FlatCellModel>();
        flattenCellModels(treeViewModel, flatModels, 0);
        setFlatModels(flatModels);
        requestLayout();
    }

    private void flattenCellModels(TreeViewModel cellModel, List<FlatCellModel> flatModels, int depth) {
        if (cellModel == null) {
            return;
        }
        FlatCellModel flatCellModel = new FlatCellModel(depth, cellModel.newViewInstance());
        flatModels.add(flatCellModel);
        List<TreeViewModel> children = cellModel.getModels();
        for (TreeViewModel child : children) {
            flattenCellModels(child, flatModels, depth + 1);
        }
    }

    private void setFlatModels(List<FlatCellModel> flatModels) {
        this.flatModels.clear();
        this.flatModels.addAll(flatModels);
        slidePanel.setupViews(flatModels);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        scrollTop.onNext(t);
    }

    static class FlatCellModel {
        int depth = 0;
        View view;

        public FlatCellModel(int depth, View view) {
            this.depth = depth;
            this.view = view;
        }
    }

    class SlidePanel extends ViewGroup {

        List<View> views = new ArrayList<View>();
        private int heightPixels;
        private int indentPixels;
        private View selectedView;

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

        public void setupViews(List<FlatCellModel> flatModels) {
            removeAllViews();
            views.clear();
            for (FlatCellModel flatModel : flatModels) {
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
            Map<Integer, Integer> previousTopAtDepth = new HashMap<Integer, Integer>();
            for (int index = views.size() - 1; index >= 0; index--) {
                View view = views.get(index);
                FlatCellModel cellModel = (FlatCellModel) view.getTag();

                int viewWidth = width - indentPixels * cellModel.depth;

                int widthMeasureSpec = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY);
                int heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightPixels, MeasureSpec.EXACTLY);
                view.measure(widthMeasureSpec, heightMeasureSpec);
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
                    viewBottom = Math.min(viewBottom, previousTop - 1);
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
