###Mojio MyFirstMojio Sample Android Application###

UNDER ACTIVE DEVELOPMENT
These instructions assume the usage of Android Studio / gradle.

This project shows how to use the Mojio SDK to perform simple OAuth2 authentication to gain access to the Mojio platform's core data. It contains all the required libs needed to build the project.

If you wish to download the full SDK source, please see [Visit our GitHub repo](https://github.com/mojio/mojio-android-sdk).

### Running this project ###
1. Fork / clone this repo
2. Open the MainActivity and add your application's MOJIO_APP_ID and REDIRECT_URL.
3. Create your own Google Maps API key and add it into the AndroidManifest.xml file (the com.google.android.maps.v2.API_KEY meta-data tag already exists, just update the key value). Please see https://developers.google.com/maps/documentation/android/ for details on creating one.
4. Build the project and run on a device!

### How OAuth2 is done in this application ###
The following steps are taken in the sample to perform basic OAuth2; if you wish to add authenication into your personal Mojio applications please follow the following steps:

1. Give your application Internet access in the AndroidManifest.xml file; if you do not do this, then the web view will fail to load with a rather undesciptive "Webpage not available" message
  
  ```
  <uses-permission android:name="android.permission.INTERNET" />
  ```

2. Your application will need the io.moj.mobile.andorid.sdk.networking.OAuthLoginActivity class to handle the OAuth2 login; add it to your app AndroidManifest.xml file:
  
  ```
  <activity android:name="io.moj.mobile.andorid.sdk.networking.OAuthLoginActivity" />
  ```

3. Create a MojioClient object using your app's keys
  
  ```
  MojioClient mMojio = new MojioClient(this, MOJIO_APP_ID, MOJIO_APP_SECRET_KEY, REDIRECT_URL);
  ```

4. Call the launchLoginActivity method passing along an Intent resultCode, so that we can correctly listen for the result.
  
  ```
  private static int OAUTH_REQUEST = 0;
  ...
  mMojio.launchLoginActivity(this, OAUTH_REQUEST);
  ```

5. Override the onActivityResult method to listen for a successful login; once we have gotten a successful result, we can move on to getting Mojio data!
  
  ```
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == OAUTH_REQUEST) {
            // We now have a stored access token
            if (resultCode == RESULT_OK) {
                // The SDK now knows our auth login - continue on to get awesome data!
            }
        }
    }
  ``` 