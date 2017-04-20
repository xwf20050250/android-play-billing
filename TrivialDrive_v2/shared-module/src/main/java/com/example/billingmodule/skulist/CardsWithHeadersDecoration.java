package com.example.billingmodule.skulist;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.example.billingmodule.skulist.row.RowDataProvider;
import com.example.billingmodule.skulist.row.SkuRowData;

/**
 * A separator for RecyclerView that keeps the specified spaces between headers and the cards.
 */
public class CardsWithHeadersDecoration extends RecyclerView.ItemDecoration {

        private final RowDataProvider mRowDataProvider;
        private final int mHeaderGap, mRowGap;

        public CardsWithHeadersDecoration(RowDataProvider rowDataProvider, int headerGap,
                int rowGap) {
            this.mRowDataProvider = rowDataProvider;
            this.mHeaderGap = headerGap;
            this.mRowGap = rowGap;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                RecyclerView.State state) {

            final int position = parent.getChildAdapterPosition(view);
            final SkuRowData data = mRowDataProvider.getData(position);

            // We should add a space on top of every header card
            if (data.getRowType() == SkusAdapter.TYPE_HEADER) {
                outRect.top = mHeaderGap;
            }

            // Adding a space under the last item
            if (position == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = mHeaderGap;
            } else {
                outRect.bottom = mRowGap;
            }
        }
}
