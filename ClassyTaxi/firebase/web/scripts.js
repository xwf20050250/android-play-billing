/**
 * Copyright 2018 Google LLC. All Rights Reserved.
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

'use strict';

// Initialize the FirebaseUI Widget using Firebase.
const ui = new firebaseui.auth.AuthUI(firebase.auth());

// Show login screen for user who hasn't logged in
const showLoginUI = () => {
  // Hide Content UI and show Login UI
  $('.content-ui').hide();
  $('.login-ui').show();

  // Configure FirebaseUI Auth
  const uiConfig = {
    signInSuccessUrl: '/',
    signInOptions: [
      firebase.auth.GoogleAuthProvider.PROVIDER_ID,
      firebase.auth.EmailAuthProvider.PROVIDER_ID,
    ],
  };

  // Show FirebaseUI login UI to user.
  ui.start('#firebaseui-auth-container', uiConfig);
}

// Show content screen for user who has logged in
const showContentUI = () => {
  // Show loading UI for both basic and premium content
  $('#spinner-basic-content').show();
  $('#spinner-premium-content').show();

  // Hide content of both basic and premium content,
  // as we need response from backend to show them
  $('#basic-content').hide();
  $('#premium-content').hide();
  $('#subscribe-requirement').hide();
  $('#subscribe-premium-requirement').hide();

  // Hide Login UI and show Content UI
  $('.login-ui').hide();
  $('.content-ui').show();

  // Attempt to load basic content
  firebase.functions().httpsCallable('content_basic')()
    .then((result) => {
      // Now we have our basic content, show it to user
      const contentUrl = result.data.url;
      $('#basic-content img').attr('src', contentUrl);
      $('#basic-content').show();
    }).catch((err) => {
      // It seems that we don't have access to content.
      // Let user know that they need to subscribe
      $('#subscribe-requirement').show();
      if (err.code !== 'permission-denied') {
        console.error('Undexpected error occurred', err);
      }
    }).finally(() => {
      // Hide loading UI elements as we have finished loading
      $('#spinner-basic-content').hide();
    });

  // Attempt to load premium content
  firebase.functions().httpsCallable('content_premium')()
    .then((result) => {
      // Now we have our premium content, show it to user
      const contentUrl = result.data.url;
      $('#premium-content img').attr('src', contentUrl);
      $('#premium-content').show();
    }).catch((err) => {
      // It seems that we don't have access to content.
      // Let user know that they need to subscribe
      $('#subscribe-premium-requirement').show();
      if (err.code !== 'permission-denied') {
        console.error('Undexpected error occurred', err);
      }
    }).finally(() => {
      // Hide loading UI elements as we have finished loading
      $('#spinner-premium-content').hide();
    });
}

// Sign the user out
const signOut = () => {
  firebase.auth().signOut();
}

// Listen to authentication state changes and update UI accordingly
firebase.auth().onAuthStateChanged(function(user) {
  if (user) {
    showContentUI();
  } else {
    showLoginUI();
  }
});
