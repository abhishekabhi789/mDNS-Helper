# mDNS Helper

mDNS Helper is an experimental Android app designed to discover local services on your Wi-Fi network. While Android 14+ devices natively support `.local` domain resolution, older devices lack this functionality. This app was created to address that limitation. Notably, even on Android 14, `.local` domain resolution does not work when using a device's hotspot instead of a Wi-Fi connection.

## Features
- **Service Discovery**: Discover services on your local network.
- **Shortcut Creation**: Create shortcut for resolved services on launcher, so that can browse the address in one click.
- **Dynamic IP Resolution**: Each time a launcher shortcut is clicked, the app will resolve the service again and fetch its latest IP address.

##   API Methods
The app uses two methods to deal with discovery and resolving the services.
1. [`NsdManager`](https://developer.android.com/reference/android/net/nsd/NsdManager): The native Android API for network service discovery.
2. [`RxDNSSD`](https://github.com/andriydruk/RxDNSSD): A third-party library that provides reliable mDNS/DNS-SD functionality.

## Limitations
- The app is in an early development stage and has not been extensively tested.
- Compatibility and reliability may vary based on the android version and device.
- NsdManager cannot resolved services discovered with RxDNSSD.
