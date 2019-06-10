---
---

{:toc}

## Getting Started

### Add the Everlytic Push Notification SDK 
1. In your module-level `build.gradle` file, add the following repository
    ```groovy
    repositories {
        // other repositories here
    
        maven {
            url  "https://dl.bintray.com/everlytic/maven"
        }
    }
    ```
1. Add the Everlytic Push Notification SDK dependency to your `build.gradle` file, replacing the `<version>` tag with the current SDK version.
    
    ```groovy
    dependencies {
        implementation 'com.everlytic.android:push-notifications:<version>'
    }
    ```
1. [Add your Firebase `google-services.json` to your project](https://firebase.google.com/docs/android/setup?authuser=0#add-config-file)

### Set Up the SDK

1. Copy your Everlytic Push Project SDK configuration string. See [Setting Up Your Everlytic Push Project](list_setup.md).
1. Add your configuration string to your `AndroidManifest.xml` 
    ```xml
    <application>
       <meta-data android:name="com.everlytic.api.SDK_CONFIGURATION" android:value="<your config string>"></meta-data>
   </application>
    ```
1. Add the following to your `Application` class
    - Kotlin 
    ```kotlin
        class App : Application() {
           override fun onCreate() {
               super.onCreate()
               
               EverlyticPush.init(this)
           }
       }
    ```
    - Java
    ```java
        import com.everlytic.android.pushnotificationsdk.EverlyticPush;
    
        public class App extends Application {
           @Override
           public void onCreate() {
               super.onCreate();
       
               EverlyticPush.init(this);
           }
       }
    ```
1. If you don't wish to add your SDK configuration string to your `AndroidManifest.xml` file, you can pass it as a second parameter to the init function. 
    ```kotlin
    EverlyticPush.init(this, "<config string>");
    ```

## Using the SDK

### Subscribing a Contact
**Kotlin**
```kotlin
fun subscribe(email: String) {

    // If you don't require a success or fail result
    EverlyticPush.subscribe(email)
    
    // Receive the subscribe result
    EverlyticPush.subscribe(email, OnResultReceiver { result ->
        // handle the subscribe result
    })
}
```

**Java**
```java
class MyClass {
    public void subscribe(String email) {
        // If you don't require a success or fail result
        EverlyticPush.subscribe(email);
        
        // Receive the subscribe result
        EverlyticPush.subscribe(email, new OnResultReceiver() {
            @Override
            public void onResult(EvResult result) {
                // handle the subscribe result
            }
        });
    }
}
```

### Unsubscribe a Contact

**Kotlin**
```kotlin
fun unsubscribe(){
// If you don't require a success or fail result
    EverlyticPush.unsubscribe(email)
    
    // Receive the subscribe result
    EverlyticPush.unsubscribe(OnResultReceiver { result ->
        // handle the subscribe result
    })
}
```

**Java**
```java
class MyClass {
    public void unsubscribe() {
        // If you don't require a success or fail result
        EverlyticPush.subscribe();
        
        // Receive the subscribe result
        EverlyticPush.unsubscribe(new OnResultReceiver() {
            @Override
            public void onResult(EvResult result) {
                // handle the subscribe result
            }
        });
    }
}
```

### Retrieve the Notification History

**Kotlin**
```kotlin
fun getNotificationHistory() {

    EverlyticPush.getNotificationHistory(OnNotificationHistoryResultListener { notifications ->
            // Handle the result set
        })

}
```

**Java**
```java
class MyClass {
    public void getNotificationHistory() {
        EverlyticPush.getNotificationHistory(new OnNotificationHistoryResultListener() {
            @Override
            public void onResult(List<EverlyticNotification> notifications) {
                
            }
        });
    }
}
```

## Basic Customization

- Change the default icon by adding a `ic_ev_notification_small`
  drawable
- Change the default colour by updating your `styles.xml` `colorPrimary`
  value