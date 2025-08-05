package com.reactnativesharemedia;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.reactnativesharemedia.NativeReactNativeShareMediaSpec;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import java.util.ArrayList;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.LifecycleEventListener;
import android.webkit.MimeTypeMap;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.OpenableColumns;

import android.widget.Toast;

public class ShareMediaModule extends NativeReactNativeShareMediaSpec implements LifecycleEventListener {
    public static final String NAME = "ReactNativeShareMedia";

    ShareMediaModule(ReactApplicationContext context) {
        super(context);
        context.addLifecycleEventListener(this);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void shareMedia(ReadableArray files, Promise promise) {
        try {
            for (int i = 0; i < files.size(); i++) {
                ReadableMap file = files.getMap(i);
                String mimeType = file.getString("mimeType");
                String data = file.getString("data");
                // ... 파일별 처리 ...
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("ERROR", e.getMessage());
        }
    }


    private String getMimeTypeFromUri(Context context, Uri uri) {
        String mimeType = null;
        if ("content".equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else if ("file".equals(uri.getScheme())) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType != null ? mimeType : "*/*";
    }

    // 파일명 추출 함수 추가
    private String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            // file:// 또는 fallback
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    result = path.substring(cut + 1);
                } else {
                    result = path;
                }
            }
        }
        return result;
    }

    @Override
    public void getSharedData(Promise promise) {
        try {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity == null) {
                promise.reject("NO_ACTIVITY", "Activity is null");
                return;
            }
            
            Intent intent = currentActivity.getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            
            // 디버깅 로그 추가
            Log.d("ShareMediaModule", "=== 디버깅 시작 ===");
            Log.d("ShareMediaModule", "Action: " + action);
            Log.d("ShareMediaModule", "Type: " + type);
            Log.d("ShareMediaModule", "Has EXTRA_STREAM: " + intent.hasExtra(Intent.EXTRA_STREAM));
            Log.d("ShareMediaModule", "Has EXTRA_TEXT: " + intent.hasExtra(Intent.EXTRA_TEXT));
            
            // 단일 파일 공유
            if (Intent.ACTION_SEND.equals(action) && type != null ) {
                Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    WritableMap result = Arguments.createMap();
                    result.putString("mimeType", type);
                    result.putString("data", fileUri.toString());
                    // 파일명 추가
                    result.putString("fileName", getFileNameFromUri(currentActivity, fileUri));

                    WritableArray resultArray = Arguments.createArray();
                    resultArray.pushMap(result);

                    promise.resolve(resultArray);

                    clearShareIntent(currentActivity);
                    return;
                }
            }

          
            
            // 여러 파일 공유 (최대 10개 제한)
            if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (fileUris != null && !fileUris.isEmpty()) {
                    WritableArray resultArray = Arguments.createArray();
                    int maxCount = Math.min(fileUris.size(), 10); // 최대 10개
                    for (int i = 0; i < maxCount; i++) {
                        Uri fileUri = fileUris.get(i);
                        WritableMap fileData = Arguments.createMap();
                        String realMimeType = getMimeTypeFromUri(currentActivity, fileUri);
                        fileData.putString("mimeType", realMimeType);
                        fileData.putString("data", fileUri.toString());
                        // 파일명 추가
                        fileData.putString("fileName", getFileNameFromUri(currentActivity, fileUri));
                        resultArray.pushMap(fileData);
                    }
                    // 11개 이상이면 JS로 Toast 이벤트 전송
                    if (fileUris.size() > 10) {
                      showNativeToast("파일은 한 번에 10개까지만 전송할 수 있습니다.");
                    }
                    promise.resolve(resultArray);
                    clearShareIntent(currentActivity);
                    return;
                }
            }

            // 텍스트,URL 공유
            if (Intent.ACTION_SEND.equals(action) && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (text != null) {
                    WritableMap result = Arguments.createMap();
                    result.putString("mimeType", type != null ? type : "text/plain");
                    result.putString("data", text);

                    WritableArray resultArray = Arguments.createArray();
                    resultArray.pushMap(result);

                    promise.resolve(resultArray);
                    clearShareIntent(currentActivity);
                    return;
                }
            }
            
            // 기타 액션들도 로그
            Log.d("ShareMediaModule", "처리되지 않은 액션: " + action);
            Log.d("ShareMediaModule", "=== 디버깅 끝 ===");
            
            promise.resolve(null);
            
        } catch (Exception e) {
            Log.e("ShareMediaModule", "Error getting shared data", e);
            promise.reject("ERROR", "Failed to get shared data", e);
        }
    }

    @Override
    public void onHostResume() {
        // 앱이 포그라운드로 돌아왔을 때 JS로 이벤트 전송
        sendEvent("onAppForeground", null);
    }

    @Override
    public void onHostPause() {}

    @Override
    public void onHostDestroy() {}

    private void sendEvent(String eventName, WritableMap params) {
        getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    // 인텐트 초기화 함수 추가
    private void clearShareIntent(Activity activity) {
        if (activity == null) return;
        Intent emptyIntent = new Intent(activity, activity.getClass());
        emptyIntent.setAction(Intent.ACTION_MAIN);
        emptyIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        activity.setIntent(emptyIntent);
    }

    //  파일 개수 초과 시 네이티브에서 바로 Toast
    private void showNativeToast(String message) {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            });
        }
    }


}