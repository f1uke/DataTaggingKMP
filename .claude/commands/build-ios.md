# Build iOS Command

Build the iOS frameworks and create XCFramework for distribution.

## Instructions

1. Build the iOS frameworks:
   ```bash
   ./gradlew :shared:linkReleaseFrameworkIosArm64 :shared:linkReleaseFrameworkIosSimulatorArm64
   ```

2. If successful, create the XCFramework:
   ```bash
   xcodebuild -create-xcframework \
     -framework shared/build/bin/iosArm64/releaseFramework/DataTaggingKMP.framework \
     -framework shared/build/bin/iosSimulatorArm64/releaseFramework/DataTaggingKMP.framework \
     -output shared/build/XCFrameworks/DataTaggingKMP.xcframework
   ```

3. Report the result to the user, including the output path if successful.
