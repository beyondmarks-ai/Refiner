# Refiner

Refiner is an Android-first Flutter app that adds a **Refine** command to
Android's text-selection menu. Selected text is sent to a dedicated Azure
Function, refined by Azure OpenAI, and returned to the original editable field.

## Use

1. Install and open the app once.
2. Type a message in a compatible Android app.
3. Select the text.
4. Open the overflow menu (⋮) if **Refine** is not immediately visible.
5. Tap **Refine**.

No Azure configuration is entered on the device.

Android apps control their own selection toolbar. Some apps or custom text fields
do not expose `ACTION_PROCESS_TEXT`, so the action cannot be guaranteed everywhere.

## Architecture

- Android registers `RefineActivity` for `ACTION_PROCESS_TEXT`.
- The app calls the dedicated Azure Function:
  `refiner-api-beyondmarks-win`.
- The Function stores the Azure OpenAI endpoint, deployment, API version, and
  model API key in server-side app settings.
- The backend calls the `gpt-4o` deployment and returns only refined text.
- The Azure OpenAI API key is never included in the APK.

This MVP embeds an Azure Function host key in the APK. That key is less privileged
than the Azure OpenAI model key, but it can still be extracted and abused to invoke
the backend. Before public distribution, replace it with user authentication or
device attestation and add per-user quotas.

A clean clone compiles with empty backend settings. For local builds, add
`refiner.backendUrl` and `refiner.backendKey` to ignored
`android/local.properties`. CI builds can instead provide
`REFINER_BACKEND_URL` and `REFINER_BACKEND_KEY` environment secrets. Never
commit either key.

## Build

```powershell
flutter pub get
flutter test
flutter build apk --release
```

The native Android integration requires Android 6.0 (API 23) or later.