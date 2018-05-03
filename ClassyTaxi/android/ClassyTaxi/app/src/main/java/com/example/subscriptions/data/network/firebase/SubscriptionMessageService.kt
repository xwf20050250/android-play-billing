/*
 * Copyright 2018 Google LLC. All rights reserved.
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

package com.example.subscriptions.data.network.firebase

import android.util.Log
import com.example.subscriptions.SubApp
import com.example.subscriptions.data.SubscriptionStatus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SubscriptionMessageService : FirebaseMessagingService() {

    companion object {
        private val TAG = SubscriptionMessageService::class.java.simpleName
        private const val REMOTE_MESSAGE_SUBSCRIPTIONS_KEY = "currentStatus"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.data?.let {
            val data = it
            if (data.isNotEmpty()) {
                var result: List<SubscriptionStatus>? = null
                if (REMOTE_MESSAGE_SUBSCRIPTIONS_KEY in data) {
                    result = data[REMOTE_MESSAGE_SUBSCRIPTIONS_KEY]?.let {
                        SubscriptionStatus.listFromJsonString(it)
                    }
                }
                if (result == null) {
                    Log.e(TAG, "Invalid subscription data")
                } else {
                    val app = application as SubApp
                    app.repository.updateSubscriptionsFromNetwork(result)
                }
            }
        }
    }
}
