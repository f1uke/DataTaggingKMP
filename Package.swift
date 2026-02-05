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
            url: "https://github.com/f1uke/DataTaggingKMP/releases/download/1.0.2/DataTaggingKMP.xcframework.zip",
            checksum: "951a6a6c7e2c4c571cdf6850a5df1ddfe19254e9458a453a5f07e8dc6f022851"
        )
    ]
)
