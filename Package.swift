// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "DataTaggingKMP",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: "DataTaggingKMP",
            targets: ["DataTaggingKMP"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "DataTaggingKMP",
            url: "https://github.com/f1uke/DataTaggingKMP/releases/download/1.0.4/DataTaggingKMP.xcframework.zip",
            checksum: "a426c01413ef23ea8020d6eb6c37ffce5f7e6eb2026981da03fefacb56df0851"
        )
    ]
)
