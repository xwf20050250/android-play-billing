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

package com.example.android.classytaxijava.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.android.classytaxijava.billing.BillingClientLifecycle;
import com.example.android.classytaxijava.data.disk.LocalDataSource;
import com.example.android.classytaxijava.data.network.WebDataSource;

import java.util.List;

public class DataRepository {
    private static DataRepository INSTANCE = null;

    private final LocalDataSource localDataSource;
    private final WebDataSource webDataSource;
    private final BillingClientLifecycle billingClientLifecycle;

    /**
     * {@MediatorLiveData} to coordinate updates from the database and the network.
     *
     * The mediator observes multiple sources. The database source is immediately exposed.
     * The network source is stored in the database, which will eventually be exposed.
     * The mediator provides an easy way for us to use LiveData for both the local data source
     * and the network data source, without implementing a new callback interface.
     */
    private MediatorLiveData<List<SubscriptionStatus>> subscriptions =
            new MediatorLiveData<>();

    private MediatorLiveData<ContentResource> basicContent = new MediatorLiveData<>();

    private MediatorLiveData<ContentResource> premiumContent = new MediatorLiveData<>();

    private DataRepository(LocalDataSource localDataSource,
                           WebDataSource webDataSource,
                           BillingClientLifecycle billingClientLifecycle) {
        this.localDataSource = localDataSource;
        this.webDataSource = webDataSource;
        this.billingClientLifecycle = billingClientLifecycle;
    }

    public LiveData<Boolean> getLoading() {
        return webDataSource.getLoading();
    }

    public MediatorLiveData<List<SubscriptionStatus>> getSubscriptions() {
        return subscriptions;
    }

    public MediatorLiveData<ContentResource> getBasicContent() {
        return basicContent;
    }

    public MediatorLiveData<ContentResource> getPremiumContent() {
        return premiumContent;
    }

    public static DataRepository getInstance(LocalDataSource localDataSource,
                                             WebDataSource webDataSource,
                                             BillingClientLifecycle billingClientLifecycle) {
        if (INSTANCE == null) {
            synchronized (DataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataRepository(
                            localDataSource, webDataSource, billingClientLifecycle);
                }
            }
        }
        return INSTANCE;
    }
}
