package com.rentall.radicalstart.util;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adds interior dividers to a RecyclerView with a LinearLayoutManager or its
 * subclass.
 */
public class DividerItemDecoration1 extends RecyclerView.ItemDecoration {

    private Drawable mDivider;
    private int mOrientation;
    private final int mLeftInset;
    private final int mRightInset;

    /**
     * Sole constructor. Takes in a {@link Drawable} to be used as the interior
     * viewholder_divider.
     *
     * @param divider A viewholder_divider {@code Drawable} to be drawn on the RecyclerView
     */
    public DividerItemDecoration1(Drawable divider) {
        mDivider = divider;
        mLeftInset = 0;
        mRightInset = 0;
    }

    /**
     * Sole constructor. Takes in a {@link Drawable} to be used as the interior
     * viewholder_divider.
     *
     * @param divider A viewholder_divider {@code Drawable} to be drawn on the RecyclerView
     * @param leftInset Left inset of a viewholder_divider
     * @param rightInset Right inset of a viewholder_divider
     */
    public DividerItemDecoration1(Drawable divider, int leftInset, int rightInset) {
        mDivider = divider;
        mLeftInset = leftInset;
        mRightInset = rightInset;
    }

    /**
     * Draws horizontal or vertical dividers onto the parent RecyclerView.
     *
     * @param canvas The {@link Canvas} onto which dividers will be drawn
     * @param parent The RecyclerView onto which dividers are being added
     * @param state The current RecyclerView.State of the RecyclerView
     */
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            drawHorizontalDividers(canvas, parent);
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVerticalDividers(canvas, parent);
        }
    }

    /**
     * Determines the size and location of offsets between items in the parent
     * RecyclerView.
     *
     * @param outRect The {@link Rect} of offsets to be added around the child
     *                view
     * @param view The child view to be decorated with an offset
     * @param parent The RecyclerView onto which dividers are being added
     * @param state The current RecyclerView.State of the RecyclerView
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }

        mOrientation = ((LinearLayoutManager) parent.getLayoutManager()).getOrientation();
        if (mOrientation == LinearLayoutManager.HORIZONTAL) {
            outRect.left = mDivider.getIntrinsicWidth();
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.top = mDivider.getIntrinsicHeight();
        }
    }

    /**
     * Adds dividers to a RecyclerView with a LinearLayoutManager or its
     * subclass oriented horizontally.
     *
     * @param canvas The {@link Canvas} onto which horizontal dividers will be
     *               drawn
     * @param parent The RecyclerView onto which horizontal dividers are being
     *               added
     */
    private void drawHorizontalDividers(Canvas canvas, RecyclerView parent) {
        int parentTop = parent.getPaddingTop();
        int parentBottom = parent.getHeight() - parent.getPaddingBottom();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int parentLeft = mLeftInset + child.getRight() + params.rightMargin;
            int parentRight = mRightInset + parentLeft + mDivider.getIntrinsicWidth();

            mDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
            mDivider.draw(canvas);
        }
    }

    /**
     * Adds dividers to a RecyclerView with a LinearLayoutManager or its
     * subclass oriented vertically.
     *
     * @param canvas The {@link Canvas} onto which vertical dividers will be
     *               drawn
     * @param parent The RecyclerView onto which vertical dividers are being
     *               added
     */
    private void drawVerticalDividers(Canvas canvas, RecyclerView parent) {
        int parentLeft = mLeftInset + parent.getPaddingLeft();
        int parentRight = mRightInset + parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int parentTop = child.getBottom() + params.bottomMargin;
            int parentBottom = parentTop + mDivider.getIntrinsicHeight();

            mDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
            mDivider.draw(canvas);
        }
    }
}