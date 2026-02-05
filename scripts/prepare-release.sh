#!/bin/bash
set -e

# Prepare XCFramework for Swift Package Manager release
# Usage: ./scripts/prepare-release.sh <version>
# Example: ./scripts/prepare-release.sh 1.0.0

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 1.0.0"
    exit 1
fi

echo "Preparing release $VERSION..."

# Clean previous build
./gradlew clean

# Build XCFramework
echo "Building XCFramework..."
./gradlew :shared:assembleXCFramework

XCFRAMEWORK_PATH="shared/build/XCFrameworks/DataTaggingKMP.xcframework"
OUTPUT_DIR="shared/build/release"
ZIP_NAME="DataTaggingKMP.xcframework.zip"

# Check if XCFramework was created
if [ ! -d "$XCFRAMEWORK_PATH" ]; then
    echo "Error: XCFramework not found at $XCFRAMEWORK_PATH"
    exit 1
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Zip the XCFramework
echo "Creating zip archive..."
cd shared/build/XCFrameworks
zip -r "../release/$ZIP_NAME" DataTaggingKMP.xcframework
cd ../../..

# Calculate checksum
echo "Calculating checksum..."
CHECKSUM=$(swift package compute-checksum "$OUTPUT_DIR/$ZIP_NAME")

echo ""
echo "=========================================="
echo "Release $VERSION prepared successfully!"
echo "=========================================="
echo ""
echo "Zip file: $OUTPUT_DIR/$ZIP_NAME"
echo "Checksum: $CHECKSUM"
echo ""
echo "Next steps:"
echo "1. Create a GitHub release with tag: $VERSION"
echo "2. Upload the zip file: $OUTPUT_DIR/$ZIP_NAME"
echo "3. Update Package.swift with:"
echo ""
echo "   .binaryTarget("
echo "       name: \"DataTaggingKMP\","
echo "       url: \"https://github.com/f1uke/DataTaggingKMP/releases/download/$VERSION/$ZIP_NAME\","
echo "       checksum: \"$CHECKSUM\""
echo "   )"
echo ""
