/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.classytaxijava.ui;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.example.android.classytaxijava.R;
import com.example.android.classytaxijava.data.ContentResource;
import com.example.android.classytaxijava.data.SubscriptionStatus;

import java.util.List;

// TODO(123725049): Improve data binding.
public class SubscriptionBindingAdapter {
    private static final String TAG = "BindingAdapter";

    /**
     * Update a loading progress bar when the status changes.
     * <p>
     * When the network state changes, the binding adapter triggers this view in the layout XML.
     * See the layout XML files for the app:loadingProgressBar attribute.
     */
    @BindingAdapter("loadingProgressBar")
    public static void loadingProgressBar(ProgressBar view, boolean loading) {
        view.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    /**
     * Update basic content when the URL changes.
     * <p>
     * When the image URL content changes, the binding adapter triggers this view in the layout XML.
     * See the layout XML files for the app:updateBasicContent attribute.
     */
    @BindingAdapter("updateBasicContent")
    public static void updateBasicContent(View view, @Nullable ContentResource basicContent) {
        ImageView image = view.findViewById(R.id.home_basic_image);
        TextView textView = view.findViewById(R.id.home_basic_text);
        String url = basicContent == null ? null : basicContent.url;
        if (url != null) {
            Log.d(TAG, "Loading image for basic content" + url);
            image.setVisibility(View.VISIBLE);
            Glide.with(view.getContext())
                    .load(url)
                    .into(image);

            textView.setText(view.getResources().getString(R.string.basic_content_text));
        } else {
            image.setVisibility(View.GONE);
            textView.setText(view.getResources().getString(R.string.no_basic_content));
        }
    }

    /**
     * Update premium content on the Premium fragment when the URL changes.
     * <p>
     * When the image URL content changes, the binding adapter triggers this view in the layout XML.
     * See the layout XML files for the app:updatePremiumContent attribute.
     */
    @BindingAdapter("updatePremiumContent")
    public static void updatePremiumContent(View view, @Nullable ContentResource premiumContent) {
        ImageView image = view.findViewById(R.id.premium_premium_image);
        TextView textView = view.findViewById(R.id.premium_premium_text);
        String url = premiumContent == null? null : premiumContent.url;
        if (url != null) {
            Log.d(TAG, "Loading image for premium content: " + url);
            image.setVisibility(View.VISIBLE);
            Glide.with(image.getContext())
                    .load(url)
                    .into(image);
            textView.setText(view.getResources().getString(R.string.premium_content_text));
        } else {
            image.setVisibility(View.GONE);
            textView.setText(view.getResources().getString(R.string.no_premium_content));
        }
    }

    /**
     * Update subscription views on the Home fragment when the subscription changes.
     * <p>
     * When the subscription changes, the binding adapter triggers this view in the layout XML.
     * See the layout XML files for the app:updateHomeViews attribute.
     */
    @BindingAdapter("updateHomeViews")
    public static void updateHomeViews(View view, List<SubscriptionStatus> subscriptions) {
        // Set visibility assuming no subscription is available.
        // If a subscription is found that meets certain criteria,
        // then the visibility of the paywall will be changed to View.GONE.
        view.findViewById(R.id.home_paywall_message).setVisibility(View.VISIBLE);
        // TODO handle restore, grace period, transfer, account hold

        view.findViewById(R.id.home_basic_message).setVisibility(View.GONE);
        // TODO Update based on subscription information.
    }

    /**
     * Update subscription views on the Premium fragment when the subscription changes.
     * <p>
     * When the subscription changes, the binding adapter triggers this view in the layout XML.
     * See the layout XML files for the app:updatePremiumViews attribute.
     */
    @BindingAdapter("updatePremiumViews")
    public static void updatePremiumViews(View view, List<SubscriptionStatus> subscriptions) {
        // TODO
    }

    /**
     * Update views on the Settings fragment when the subscription changes.
     * <p>
     * When the subscription changes, the binding adapter triggers this view in the layout XML.
     * See the layout XML files for the app:updateSettingsViews attribute.
     */
    @BindingAdapter("updateSettingsViews")
    public static void updateSettingsViews(View view, List<SubscriptionStatus> subscriptions) {
        // TODO
    }

    /**
     * Get a readable expiry date from a subscription.
     */
    public static String getHumanReadableExpiryDate(SubscriptionStatus subscription) {
        // TODO
        return "";
    }
}
