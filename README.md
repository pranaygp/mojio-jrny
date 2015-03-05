# How to create "My First Mojio" Android application

### Requirements
1. **Android Studio**: Android Studio is now the official IDE and developer tools for building Applications. Because of this, these instructions assume the usage of Android Studio. Please visit http://developer.android.com/sdk/index.html for more details.

### Using the Mojio Android SDK
*Note: Currently project is downloaded as the full source, eventually we will want create the project as as AAR file and host it on one of the library repositories.*

1. Download the Android Mojio SDK project (or AAR file)

2. Create a folder for the project (Example: my-first-mojio)

3. Open Android studio and create a new Android project in that folder (Example: MyFirstMojio)

4. Import the SDK; there are a few ways to do this (depending on if you have the AAR file, or the entire SDK source):
  1. Use the AAR library file (ie. not the entire project source)
    * Copy the mojiosdksrc-1.0.0.aar file into your project's app/libs folder
    * Update your app's build.gradle script to use include the libs folder as a repository:
      ```
      repositories {
        flatDir {
          dirs 'libs'
        }
      }  
      ```
    * Update your app's build.gradle script to include the AAR as well as the SDKs dependancies (make sure to resolve any library conflicts):
      ```
      dependencies {
        ...
        // Mojio SDK dependencies
        compile 'com.google.code.gson:gson:2.2.4'
        compile 'com.mcxiaoke.volley:library:1.0.+'
        compile 'joda-time:joda-time:2.5'
        compile 'com.mojio.mojiosdk:mojiosdksrc:1.0.0@aar'
      }
      ```
    * Note: Long term goal is to place this library in a central maven repo; when this occurs you will no longer have to manually add these SDK dependencies.
    * Sync your gradle file
    * If successful, no errors should occur. 
    * If ClassNoDefFoundErrors are found while using the Mojio SDK, then these dependencies were not properly resolved.
  2. Linking to the downloaded SDK source project directly (preferred); this method allows you to use the same copy of the SDK source in multiple projects. SDK updates will be easier to maintain this way.
    * To do this, you add the SDK as a project in your project's settings.gradle file; note that this will contain the relative path to the mojiosdksrc module, which may be different than the path below.
      ```
      include ':app', ':mojiosdksrc'
      project(':mojiosdksrc').projectDir = new File(settingsDir, '../../mojio-android-sdk/MojioSDK/mojiosdksrc')
      ```
    * If successful, a mojiosdksrc folder will appear in your project; opening the folder will show the SDK source.
    * Add the module (':mojiosdksrc') to the app's build.gradle script:
      ``` 
      dependencies {
        // ... 
        compile project(':mojiosdksrc')
      }
      ```
  3. Importing a *copy* of the source from the downloaded SDK; this method will still work, but will cause multiple copies of the SDK source to be created in each one of your projects. SDK updates will be harder to maintain.
    * Select File ==> Import Module
    * Navigate to the downloaded SDK and select the "mojiosdksrc" module from the SDK (ie. ../mojio-android-sdk/MojioSDK/mojiosdksrc). 
    * If you select the project itself and not the module, this will not work.
    * If successful, a mojiosdksrc folder will appear in your project; opening the folder will show the SDK source.
    * Add the module (':mojiosdksrc') to the app's build.gradle script:
      ``` 
      dependencies {
        // ... 
        compile project(':mojiosdksrc')
      }
      ```
5. Re-sync the gradle scripts to finish loading the SDK module
6. Now the SDK (including the MojioClient object) can be used in your app!

### To use the SDK's OAuth2 Login Activity for Authentication
1. Give your application Internet access in the AndroidManifest.xml file; if you do not do this, then the web view will fail to load with a rather undesciptive "Webpage not available" message
  ```
  <uses-permission android:name="android.permission.INTERNET" />
  ```
2. Your application will need the com.mojio.mojiosdk.networking.OAuthLoginActivity class to handle the OAuth2 login; add it to your app AndroidManifest.xml file:
  ```
  <activity android:name="com.mojio.mojiosdk.networking.OAuthLoginActivity" />
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
