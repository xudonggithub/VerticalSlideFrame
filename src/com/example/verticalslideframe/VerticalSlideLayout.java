package com.example.verticalslideframe;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


public class VerticalSlideLayout extends ViewGroup {
	enum ScrollerLocation {
		Top, Bottom, Other
	};
	private ViewDragHelper mDragHelper;
	private View topView, bottomView;
	private int topViewTop;
	private int topViewTopBound, topViewBottomBound;
	private boolean initedLayoutParam =false;
	private ScrollerLocation location = ScrollerLocation.Other;
	
	public VerticalSlideLayout(Context context) {
		this(context, null);
	}
	public VerticalSlideLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public VerticalSlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
		
	}
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new VerticalSlideLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new VerticalSlideLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new VerticalSlideLayoutParams(getContext(), attrs);
    }
    
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof VerticalSlideLayoutParams && super.checkLayoutParams(p);
    }
    
	@Override
	protected void onFinishInflate() {
		topView = findViewById(R.id.top_view);
		bottomView = findViewById(R.id.bottom_view);
		bottomView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP)
					v.performClick();
				return true;
			}
		});
	}
	
	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int childCount = getChildCount();
		float maxWeight = 0;
		if (!initedLayoutParam) {
			float sumWeight = 0;
			for (int i = 0; i < childCount; i++) {
				View childView = getChildAt(i);
				VerticalSlideLayoutParams lp = (VerticalSlideLayoutParams) childView
						.getLayoutParams();
				sumWeight += lp.weight;
				if (maxWeight < lp.weight)
					maxWeight = lp.weight;
			}
			if (sumWeight > 0) {
				if (heightMode != MeasureSpec.EXACTLY)
					throw new IllegalStateException(
							"Height must be MATCH_PARENT for layout_weight.");
				for (int i = 0; i < childCount; i++) {
					View childView = getChildAt(i);
					VerticalSlideLayoutParams lp = (VerticalSlideLayoutParams) childView
							.getLayoutParams();
					float weight = lp.weight;
					int childHeight = (int) (heightSize * (maxWeight / sumWeight));// ignore  margins
					if (childView == topView) {
						topViewTopBound = (int) (heightSize * (weight / sumWeight));
						topViewBottomBound = (int) childHeight;
						topViewTop = childHeight;
					}
					int childHeightSpec = MeasureSpec.makeMeasureSpec(
							childHeight, MeasureSpec.EXACTLY);
					int childWidthSpec;
					if (lp.width == LayoutParams.WRAP_CONTENT) {
						childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
								MeasureSpec.AT_MOST);
					} else if (lp.width == LayoutParams.MATCH_PARENT) {
						childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
								MeasureSpec.EXACTLY);
					} else {
						childWidthSpec = MeasureSpec.makeMeasureSpec(lp.width,
								MeasureSpec.EXACTLY);
					}
					childView.measure(childWidthSpec, childHeightSpec);
				}
			} else
				measureChildren(widthMeasureSpec, heightMeasureSpec);

			initedLayoutParam = true;
		}else {
			for (int i = 0; i < childCount; i++) {
				View childView = getChildAt(i);
				VerticalSlideLayoutParams lp = (VerticalSlideLayoutParams) childView.getLayoutParams();
				int childHeightSpec = 0, childWidthSpec = 0;
				if(childView == bottomView) {
					childHeightSpec = MeasureSpec.makeMeasureSpec(
							 topViewTop, MeasureSpec.EXACTLY);
				}
				else if(childView == topView) {
					childHeightSpec = MeasureSpec.makeMeasureSpec(
							heightSize - topViewTop, MeasureSpec.EXACTLY);
				}
				if (lp.width == LayoutParams.WRAP_CONTENT) {
					childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
							MeasureSpec.AT_MOST);
				} else if (lp.width == LayoutParams.MATCH_PARENT) {
					childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize,
							MeasureSpec.EXACTLY);
				} else {
					childWidthSpec = MeasureSpec.makeMeasureSpec(lp.width,
							MeasureSpec.EXACTLY);
				}
				childView.measure(childWidthSpec, childHeightSpec);
				
			}
		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
//		int childTop = 0;
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);
			final int childWidth = childView.getMeasuredWidth();
            final int childHeight = childView.getMeasuredHeight();
			MarginLayoutParams lp = (MarginLayoutParams)childView.getLayoutParams();
			if(childView == bottomView) {
				childView.layout(l, t, l + childWidth, t + childHeight);
			}
			else if(childView == topView) {
				childView.layout(l, topViewTop, l + childWidth, topViewTop + childHeight);
			}
			
		}
	}
	
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }
        boolean ret = mDragHelper.shouldInterceptTouchEvent(ev);
        return ret || checkTouchEventEccepted(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//    	if(checkTouchEventEccepted(ev))
    		mDragHelper.processTouchEvent(ev);
        return true;
    }

    private boolean checkTouchEventEccepted(MotionEvent ev) {
    	 boolean isUnderTop = mDragHelper.isViewUnder(topView, (int) ev.getX(), (int) ev.getY());
         boolean isTopCanMoved = isUnderTop && location != ScrollerLocation.Top;
         boolean isUnderBottom = mDragHelper.isViewUnder(bottomView, (int) ev.getX(), (int) ev.getY());
         boolean isBottomCanMoved = isUnderBottom && location != ScrollerLocation.Bottom;
         System.out.println("cxd,isTopCanMoved="+isTopCanMoved+",isBottomCanMoved="+isBottomCanMoved+",location="+location);
         return isTopCanMoved || isBottomCanMoved;
    }
	
	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public boolean tryCaptureView(View arg0, int arg1) {
			return arg0 == topView || arg0 == bottomView;
		}
		
		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			int newTop = 0;
			if (child == topView) {
				newTop = Math.min(Math.max(top, topViewTopBound), topViewBottomBound);
				
			} else if (child == bottomView) {
				topViewTop += dy;
				newTop = Math.min(Math.max(topViewTop, topViewTopBound), topViewBottomBound);
				topViewTop = newTop;
			}
			
			return newTop;
		}

		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			if(changedView == topView)
				topViewTop = top;
			if (top == topViewBottomBound){
				location = ScrollerLocation.Bottom;
				System.out.println("cxd, location set to Bottom");
			}
			else if (top == topViewTopBound) {
				location = ScrollerLocation.Top;
				System.out.println("cxd, location set to Top");
			}
			
			requestLayout();
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			if(releasedChild == bottomView)
			{
				if(yvel > 0)
					mDragHelper.smoothSlideViewTo(topView, topView.getLeft(), topViewBottomBound);
				else if(yvel < 0)
					mDragHelper.smoothSlideViewTo(topView, topView.getLeft(), topViewTopBound);
			}
			else if(releasedChild == topView) {
	 			if(yvel > 0) 
					mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), topViewBottomBound);
				else if(yvel < 0)
					mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), topViewTopBound);
				}
			invalidate();
		}
	}
	
	public static class VerticalSlideLayoutParams extends MarginLayoutParams {

		private static final int[] ATTRS = new int[] { android.R.attr.layout_weight };

		private float weight = 0;

		public VerticalSlideLayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
			this.weight = a.getFloat(0, 0);
			a.recycle();
		}

		public VerticalSlideLayoutParams(VerticalSlideLayoutParams source) {
			super(source);
			this.weight = source.weight;
		}

		public VerticalSlideLayoutParams(MarginLayoutParams source) {
			super(source);
		}

		public VerticalSlideLayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

		public VerticalSlideLayoutParams(int width, int height) {
			super(width, height);
		}

	}
}
