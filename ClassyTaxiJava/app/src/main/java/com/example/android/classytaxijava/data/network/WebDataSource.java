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

package com.example.android.classytaxijava.data.network;

import androidx.lifecycle.LiveData;

import com.example.android.classytaxijava.AppExecutors;
import com.example.android.classytaxijava.data.network.firebase.ServerFunctions;

import java.util.concurrent.Executor;

public class WebDataSource {
    private static volatile WebDataSource INSTANCE = null;
    private final Executor executor;
    private final ServerFunctions serverFunctions;

    private WebDataSource(Executor executor, ServerFunctions serverFunctions) {
        this.executor = executor;
        this.serverFunctions = serverFunctions;
    }

    public LiveData<Boolean> getLoading() {
        return serverFunctions.getLoading();
    }

    public static WebDataSource getInstance(AppExecutors executors,
                                            ServerFunctions callableFunctions) {
        if (INSTANCE == null) {
            synchronized (WebDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WebDataSource(executors.networkIO, callableFunctions);
                }
            }
        }
        return INSTANCE;
    }
}
