# Changelog
All notable changes to this project will be documented in this file.

## [3.0.2](https://github.com/dynata/inBrainsurveys-android/releases/tag/3.0.2) - 2025-09-26
### Added
- README.MD CHANGELOG.MD and License now part of the repository;

### Changed
- Moved to the new repository, https://github.com/dynata/inBrainsurveys-android
---

## [3.0.1](https://www.jitpack.io/#inbrainai/sdk-android/3.0.1) - 2025-04-17

### Fixed
- Fixed `Toolbar` issue overlapped by `StatusBar`
---

## [3.0.0](https://www.jitpack.io/#inbrainai/sdk-android/3.0.0) - 2025-04-01

### Changed
- Upgrade `minSdk` version: 16 -> 21
- Upgrade `dependencies` versions
  - `androidx.core:core-ktx`: `1.10.1` -> `1.15.0`
  - `androidx.webkit:webkit`: `1.4.0` -> `1.12.1`
- Remove deprecated APIs in `InBrainCallback`
  - `default void surveysClosed()`
  - `default void surveysClosedFromPage()`
- Remove deprecated APIs in `InBrain` class
  - `public void setInBrainValuesFor(String sessionID, HashMap<String, String> dataOptions)`
  - `public void setLanguage(String language)`
  - `public void showSurveys(Context context, final StartSurveysCallback callback)`
  - `public void showNativeSurvey(Context context, Survey survey, final StartSurveysCallback callback)`
  - `public void showNativeSurveyWith(Context context, String surveyId, String searchId, final StartSurveysCallback callback)`
- Remove deprecated variable in `Survey` class
  - `public int conversionThreshold`
---

## [2.5.2](https://www.jitpack.io/#inbrainai/sdk-android/2.5.2) - 2025-03-25

### Changed
- Default option for `showSurveys(Context context, fianl StartSurveyCallback callback)` is `WallOption.SURVEYS`
- Disable the `offers` for `showNativeSurvey(Context context, Survey survey, final StartSurveysCallback callback)` function
- Disable the `offers` for `showNativeSurveyWith(Context context, String surveyId, String searchId, final StartSurveysCallback callback)` function
---

## [2.5.1](https://www.jitpack.io/#inbrainai/sdk-android/2.5.1) - 2025-02-25

### Fixed
- Fixed the usage of a deprecated method.
  `setStatusBarColor(int)` method
---

## [2.5.0](https://www.jitpack.io/#inbrainai/sdk-android/2.5.0) - 2024-11-18

### Added/Fixed
- Added an ability to show only surveys or only offers at the inBrain Wall.
  `WallOption` enum
  `openWall(Context, WallOption, StartSurveysCallback)` method
  `showNativeSurvey(Context, Survey, boolean, StartSurveysCallback)` method
  `showNativeSurveyWith(Context, String, String, boolean, StartSurveysCallback)` method
- Deprecated some old methods.
  `showSurveys(Context, StartSurveysCallback)` method
  `showNativeSurvey(Context, Survey, StartSurveysCallback)` method
  `showNativeSurveyWith(Context, String, String, StartSurveysCallback)` method
---

## [2.4.5](https://www.jitpack.io/#inbrainai/sdk-android/2.4.5) - 2024-09-19

### Added/Fixed
- Added support for the new `Offers` feature.
- Disallow insecure network traffic.
---

## [2.4.3](https://www.jitpack.io/#inbrainai/sdk-android/2.4.3) - 2024-07-31

### Fixed
- Performance and stability improvements.
---

## [2.4.0](https://www.jitpack.io/#inbrainai/sdk-android/2.4.0) - 2024-01-22

### Added
- Support of Panelist (Dynamic) Currency Sales - the SDK returns the active sale with the highest multiplier across all active Publisher and Panelist sales.
---

## [2.3.0](https://www.jitpack.io/#inbrainai/sdk-android/2.3.0) - 2024-01-04

### Fixed
- Fixed a few possible crashes including one that might happen when WebView package is not installed
---

## [2.2.0](https://www.jitpack.io/#inbrainai/sdk-android/2.2.0) - 2023-10-11

### Added
- Added a new flag (`isProfilerSurvey`) to the `Survey` object
---

## [2.1.18](https://www.jitpack.io/#inbrainai/sdk-android/2.1.18) - 2023-08-08

### Fixed
- Updated the ProGuard rule to deobfuscate public method parameters
---

## [2.1.17](https://www.jitpack.io/#inbrainai/sdk-android/2.1.17) - 2023-07-18

### Fixed
- Fixed a crash of `getRewards()`
---

## [2.1.16](https://www.jitpack.io/#inbrainai/sdk-android/2.1.16) - 2023-06-26

### Fixed
- Fixed a gradle build issue cased by the dependency `androidx.core:core-ktx`
---

## [2.1.15](https://www.jitpack.io/#inbrainai/sdk-android/2.1.15) - 2023-06-07

### Fixed
- Fixed a crash when initializing the SDK from non-main thread
---

## [2.1.14](https://www.jitpack.io/#inbrainai/sdk-android/2.1.14) - 2023-05-25

### Fixed
- Force fire callback even if client/secret is invalid
- Fix some parameter casting issues
---

## [2.1.13](https://www.jitpack.io/#inbrainai/sdk-android/2.1.13) - 2023-05-22

### Fixed
- Fixed an issue parsing parameters of `getNativeSurveys` request
---

## [2.1.12](https://www.jitpack.io/#inbrainai/sdk-android/2.1.12) - 2023-05-16

### Fixed
- Update surveys availability checking logic
- Refactor and cleanup codes
---

## [2.1.6](https://www.jitpack.io/#inbrainai/sdk-android/2.1.6) - 2023-02-13

### Fixed
- Fixed an issue accessing Kotlin code from C#
---

## [2.1.5](https://www.jitpack.io/#inbrainai/sdk-android/2.1.5) - 2023-01-30

### Added/Fixed
- Include a sub module example for easier test purpose.
- Remove useless logic regarding confirmedRewardsIds and lastReceivedRewards.
- Make didReceiveInBrainRewards() optional.
- Adjust default toolbar and statusbar configs.
- Make it possible to init InBrain outside main thread.
---

## [2.1.2](https://www.jitpack.io/#inbrainai/sdk-android/2.1.2) - 2023-01-03

### Added/Fixed
- Added option to setup SDK without `userId`.
- Added option to set `userId` separately from SDK setup.
- Added info about survey's conversion level.
- Added option to confirm rewards by transactionIds.
- Added option to setup `sessionId` and `dataOptions` separately.
- Added new callback with information about reward for NativeSurveys.
- Deprecated some old functions and properties.
---

## [2.0.0](https://www.jitpack.io/#inbrainai/sdk-android/2.0.0) - 2022-10-30

### Added/Fixed
- Added support for categories.
- Improved usage of native surveys methods.
---

## [1.0.28](https://www.jitpack.io/#inbrainai/sdk-android/1.0.28) - 2022-10-24

### Fixed
- Fixed the proguard rule.
---

## [1.0.27](https://www.jitpack.io/#inbrainai/sdk-android/1.0.27) - 2022-09-13

### Fixed
- Changed the redirect URL and dialog content when clicking back button to exit a survey.
---

## [1.0.26](https://www.jitpack.io/#inbrainai/sdk-android/1.0.26) - 2022-09-05

### Fixed
- Show a loading indicator when the survey WebView is being initialized.
---

## [1.0.25](https://www.jitpack.io/#inbrainai/sdk-android/1.0.25) - 2022-07-03

### Added
- Added support for profile match. Each survey now has `conversionThreshold` field which reflects conversion rates.
---

## [1.0.24](https://www.jitpack.io/#inbrainai/sdk-android/1.0.24) - 2022-06-15

### Added
- Added support for some specific apps to fetch ongoing currency sale details.
---

## [1.0.23](https://www.jitpack.io/#inbrainai/sdk-android/1.0.23) - 2022-05-31

### Added/Fixed
- Getters for 2 InBrain properties (`sessionUid` and `dataOptions`) have been exposed for convenience.
- Upgraded AGP to latest and added `maven-publish` plugin to resolve some JitPack build warnings.
---

## [1.0.21](https://www.jitpack.io/#inbrainai/sdk-android/1.0.21) - 2021-12-13

### Added
- 2 additional properties (`currencySale` and `multiplier`) have become available for native surveys.
---

## [1.0.20](https://www.jitpack.io/#inbrainai/sdk-android/1.0.20) - 2021-11-24

### Fixed
- Fixed the issue where we were often falling into a blank screen when trying to enter an inBrain survey.
---

## [1.0.19](https://www.jitpack.io/#inbrainai/sdk-android/1.0.19) - 2021-10-04

### Fixed
- Enabled `setLanguage()` back.
---

## [1.0.18](https://www.jitpack.io/#inbrainai/sdk-android/1.0.18) - 2021-09-23

### Added
- Add support for passing in an optional parameter `placement_id` to the native surveys APIs.
---

## [1.0.16](https://www.jitpack.io/#inbrainai/sdk-android/1.0.16) - 2020-01-21

### Added
- Add support for multiple windows for webview.
---

## [1.0.15](https://www.jitpack.io/#inbrainai/sdk-android/1.0.15) - 2021-01-12

### Added
- Added support for nativeSurveyClosed message.
---

## [1.0.14](https://www.jitpack.io/#inbrainai/sdk-android/1.0.14) - 2020-12-11

### Fixed
- Inverted light status bar icons color.
---

## [1.0.13](https://www.jitpack.io/#inbrainai/sdk-android/1.0.13) - 2020-12-11
 
### Added
- Navigation to feedback after aborting survey.

## [1.0.12](https://www.jitpack.io/#inbrainai/sdk-android/1.0.12) - 2020-12-03

### Added
- inBrain V2 customization.
---

## [1.0.9](https://www.jitpack.io/#inbrainai/sdk-android/1.0.9) - 2020-11-02

### Added
- Added native surveys.
---
