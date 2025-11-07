# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and `InBrainSurveys` adheres to [Semantic Versioning](http://semver.org/).

## [3.1.1](https://github.com/dynata/inBrainsurveys-android/releases/tag/3.1.1) - 2025-11-07

### Added

- **Native Offers Support:**
    - `InBrainNativeOffer` - class representing a native offer
    - `InBrainOfferFilter` - class for filtering native offers
    - `InBrainOfferType` - enum used to specify which offers to fetch from API using the filter
    - `InBrainOfferGoal` - class representing offer goals
    - `InBrainOfferPromotion` - class representing offer promotions
    - `GetNativeOffersCallback` - callback interface for handling native offers responses
    - `OpenOfferCallback` - callback interface for handling offer opening responses
    - `getNativeOffers(InBrainOfferFilter, GetNativeOffersCallback)` method to `InBrain` for fetching native offers
    - `openOfferWithId(int, Context, OpenOfferCallback)` method to `InBrain` for opening specific offers

### Changed

- Refactored internal API and authentication layers for better performance and stability

## [3.0.2](https://github.com/dynata/inBrainsurveys-android/releases/tag/3.0.2) - 2025-09-26

### Added

- README.md, CHANGELOG.md and License files now part of the repository

### Changed

- Moved to the new repository: https://github.com/dynata/inBrainsurveys-android

## [3.0.1](https://www.jitpack.io/#inbrainai/sdk-android/3.0.1) - 2025-04-17

### Fixed

- Fixed `Toolbar` issue overlapped by `StatusBar`

## [3.0.0](https://www.jitpack.io/#inbrainai/sdk-android/3.0.0) - 2025-04-01

### Changed

- Upgraded `minSdk` version from 16 to 21
- Upgraded dependencies:
    - `androidx.core:core-ktx` from `1.10.1` to `1.15.0`
    - `androidx.webkit:webkit` from `1.4.0` to `1.12.1`

### Removed

- Deprecated APIs in `InBrainCallback`:
    - `default void surveysClosed()`
    - `default void surveysClosedFromPage()`
- Deprecated APIs in `InBrain` class:
    - `public void setInBrainValuesFor(String sessionID, HashMap<String, String> dataOptions)`
    - `public void setLanguage(String language)`
    - `public void showSurveys(Context context, final StartSurveysCallback callback)`
    - `public void showNativeSurvey(Context context, Survey survey, final StartSurveysCallback callback)`
    - `public void showNativeSurveyWith(Context context, String surveyId, String searchId, final StartSurveysCallback callback)`
- Deprecated variable in `Survey` class:
    - `public int conversionThreshold`

## [2.5.2](https://www.jitpack.io/#inbrainai/sdk-android/2.5.2) - 2025-03-25

### Changed

- Default option for `showSurveys(Context context, final StartSurveyCallback callback)` is now `WallOption.SURVEYS`
- Disabled the `offers` for `showNativeSurvey(Context context, Survey survey, final StartSurveysCallback callback)` function
- Disabled the `offers` for `showNativeSurveyWith(Context context, String surveyId, String searchId, final StartSurveysCallback callback)` function

## [2.5.1](https://www.jitpack.io/#inbrainai/sdk-android/2.5.1) - 2025-02-25

### Fixed

- Fixed the usage of deprecated `setStatusBarColor(int)` method

## [2.5.0](https://www.jitpack.io/#inbrainai/sdk-android/2.5.0) - 2024-11-18

### Added

- Ability to show only surveys or only offers at the inBrain Wall
- `WallOption` enum
- `openWall(Context, WallOption, StartSurveysCallback)` method
- `showNativeSurvey(Context, Survey, boolean, StartSurveysCallback)` method
- `showNativeSurveyWith(Context, String, String, boolean, StartSurveysCallback)` method

### Deprecated

- `showSurveys(Context, StartSurveysCallback)` method
- `showNativeSurvey(Context, Survey, StartSurveysCallback)` method
- `showNativeSurveyWith(Context, String, String, StartSurveysCallback)` method

## [2.4.5](https://www.jitpack.io/#inbrainai/sdk-android/2.4.5) - 2024-09-19

### Added

- Support for the new `Offers` feature

### Changed

- Disallowed insecure network traffic

## [2.4.3](https://www.jitpack.io/#inbrainai/sdk-android/2.4.3) - 2024-07-31

### Fixed

- Performance and stability improvements

## [2.4.0](https://www.jitpack.io/#inbrainai/sdk-android/2.4.0) - 2024-01-22

### Added

- Support for Panelist (Dynamic) Currency Sales - the SDK now returns the active sale with the highest multiplier across all active Publisher and Panelist sales

## [2.3.0](https://www.jitpack.io/#inbrainai/sdk-android/2.3.0) - 2024-01-04

### Fixed

- Fixed several possible crashes including one that might happen when WebView package is not installed

## [2.2.0](https://www.jitpack.io/#inbrainai/sdk-android/2.2.0) - 2023-10-11

### Added

- New flag `isProfilerSurvey` to the `Survey` object

## [2.1.18](https://www.jitpack.io/#inbrainai/sdk-android/2.1.18) - 2023-08-08

### Fixed

- Updated the ProGuard rule to deobfuscate public method parameters

## [2.1.17](https://www.jitpack.io/#inbrainai/sdk-android/2.1.17) - 2023-07-18

### Fixed

- Fixed a crash in `getRewards()` method

## [2.1.16](https://www.jitpack.io/#inbrainai/sdk-android/2.1.16) - 2023-06-26

### Fixed

- Fixed a Gradle build issue caused by the dependency `androidx.core:core-ktx`

## [2.1.15](https://www.jitpack.io/#inbrainai/sdk-android/2.1.15) - 2023-06-07

### Fixed

- Fixed a crash when initializing the SDK from non-main thread

## [2.1.14](https://www.jitpack.io/#inbrainai/sdk-android/2.1.14) - 2023-05-25

### Fixed

- Force fire callback even if client/secret is invalid
- Fixed some parameter casting issues

## [2.1.13](https://www.jitpack.io/#inbrainai/sdk-android/2.1.13) - 2023-05-22

### Fixed

- Fixed an issue parsing parameters of `getNativeSurveys` request

## [2.1.12](https://www.jitpack.io/#inbrainai/sdk-android/2.1.12) - 2023-05-16

### Changed

- Updated surveys availability checking logic
- Refactored and cleaned up code

## [2.1.6](https://www.jitpack.io/#inbrainai/sdk-android/2.1.6) - 2023-02-13

### Fixed

- Fixed an issue accessing Kotlin code from C#

## [2.1.5](https://www.jitpack.io/#inbrainai/sdk-android/2.1.5) - 2023-01-30

### Added

- Sub module example for easier testing purposes
- Ability to initialize InBrain outside main thread

### Changed

- Made `didReceiveInBrainRewards()` optional
- Adjusted default toolbar and statusbar configurations

### Removed

- Removed useless logic regarding `confirmedRewardsIds` and `lastReceivedRewards`

## [2.1.2](https://www.jitpack.io/#inbrainai/sdk-android/2.1.2) - 2023-01-03

### Added

- Option to setup SDK without `userId`
- Option to set `userId` separately from SDK setup
- Information about survey's conversion level
- Option to confirm rewards by transaction IDs
- Option to setup `sessionId` and `dataOptions` separately
- New callback with information about reward for Native Surveys

### Deprecated

- Some old functions and properties

## [2.0.0](https://www.jitpack.io/#inbrainai/sdk-android/2.0.0) - 2022-10-30

### Added

- Support for categories

### Changed

- Improved usage of native surveys methods

## [1.0.28](https://www.jitpack.io/#inbrainai/sdk-android/1.0.28) - 2022-10-24

### Fixed

- Fixed the ProGuard rule

## [1.0.27](https://www.jitpack.io/#inbrainai/sdk-android/1.0.27) - 2022-09-13

### Changed

- Changed the redirect URL and dialog content when clicking back button to exit a survey

## [1.0.26](https://www.jitpack.io/#inbrainai/sdk-android/1.0.26) - 2022-09-05

### Added

- Loading indicator when the survey WebView is being initialized

## [1.0.25](https://www.jitpack.io/#inbrainai/sdk-android/1.0.25) - 2022-07-03

### Added

- Support for profile match - each survey now has `conversionThreshold` field which reflects conversion rates

## [1.0.24](https://www.jitpack.io/#inbrainai/sdk-android/1.0.24) - 2022-06-15

### Added

- Support for some specific apps to fetch ongoing currency sale details

## [1.0.23](https://www.jitpack.io/#inbrainai/sdk-android/1.0.23) - 2022-05-31

### Added

- Exposed getters for 2 InBrain properties (`sessionUid` and `dataOptions`) for convenience

### Changed

- Upgraded AGP to latest version and added `maven-publish` plugin to resolve some JitPack build warnings

## [1.0.21](https://www.jitpack.io/#inbrainai/sdk-android/1.0.21) - 2021-12-13

### Added

- 2 additional properties (`currencySale` and `multiplier`) available for native surveys

## [1.0.20](https://www.jitpack.io/#inbrainai/sdk-android/1.0.20) - 2021-11-24

### Fixed

- Fixed the issue where users were often falling into a blank screen when trying to enter an inBrain survey

## [1.0.19](https://www.jitpack.io/#inbrainai/sdk-android/1.0.19) - 2021-10-04

### Fixed

- Re-enabled `setLanguage()` method

## [1.0.18](https://www.jitpack.io/#inbrainai/sdk-android/1.0.18) - 2021-09-23

### Added

- Support for passing in an optional parameter `placement_id` to the native surveys APIs

## [1.0.16](https://www.jitpack.io/#inbrainai/sdk-android/1.0.16) - 2020-01-21

### Added

- Support for multiple windows for WebView

## [1.0.15](https://www.jitpack.io/#inbrainai/sdk-android/1.0.15) - 2021-01-12

### Added

- Support for `nativeSurveyClosed` message

## [1.0.14](https://www.jitpack.io/#inbrainai/sdk-android/1.0.14) - 2020-12-11

### Fixed

- Inverted light status bar icons color

## [1.0.13](https://www.jitpack.io/#inbrainai/sdk-android/1.0.13) - 2020-12-11

### Added

- Navigation to feedback after aborting survey

## [1.0.12](https://www.jitpack.io/#inbrainai/sdk-android/1.0.12) - 2020-12-03

### Added

- inBrain V2 customization

## [1.0.9](https://www.jitpack.io/#inbrainai/sdk-android/1.0.9) - 2020-11-02

### Added

- Native surveys support
