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
            url: "https://github.com/f1uke/DataTaggingKMP/releases/download/1.0.6/DataTaggingKMP.xcframework.zip",
            checksum: "0d1f717d0080ddca5ddfef2f630904984877f0917b9e484bcdb66bfb0f3924a5"
        )
    ]
)
