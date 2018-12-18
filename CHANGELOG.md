# CHANGELOG

## 4.7

  - Fixed network TLS support for devices 16 <= API <= 21
  - Fixed fetching strip title in some situations (was blocking widget also)
  - Replaced HttpClient with Volley and custom HurlStack for TLS handling

## 4.6

  - Updated SDK to v28
  - Fixed sharing strip and text only, thanks to @oldlauer
  - Fixed parsing remote HTML, thanks to @3DES
  - Fixed caching of preferences between processes
  - Fixed scaling of image, thanks to @jazzzz

## 4.5

  - New parsing (using twitter:image and twitter:title, for simplicity)
  - Downloading and saving strip title (only on refresh for already cached strips)
  - Displaying title in basic browsing and in widget (with option to turn off for widget in preferences)
  - Updated SDK to v27
  - Raised minimum Android API version from 9 to 14 (sorry, phones with 7 years obsolete OS)

## 4.4

  - Added offline mode - browsing only downloaded images
  - Added random feature when browsing favorites or offline
  - Updated SDK to v25
  - Function to open current strip in browser
  - Fixed handling storage permission on v23+
  - Added option to export all strip urls with respective dates as text
  - Fixed share function, that was previously broken because of Glide library

## 4.3

  - Compatibility with Android 6
  - Migrated from Android UIL to Glide library
  - Fixed some typos and translation issues

## 4.2

  - Fixed crashes on missing network connection

## 4.1

  - Removed settings "High quality", "Mobile networks"
  - Now compatible with new dilbert.com website design
  - Refresh now removes cached image in both memory and disk (fixes problem of broken image downloads)
  - Image is now loaded directly, if the URL is already cached, not waiting/spawning thread in AsyncTask
  - Handling new dilbert.com URL pattern

## 4.0

  - Support for Android Lollipop (5.0)
  - Material Design
  - Dark backgrounds by default
  - Fixed crash in changing download directory
