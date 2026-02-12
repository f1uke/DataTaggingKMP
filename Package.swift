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
            url: "https://github.com/f1uke/DataTaggingKMP/releases/download/1.0.8/DataTaggingKMP.xcframework.zip",
            checksum: "84f11dc927d773c1bd8cd13c3135dcf2fdff3b573f6d8d138f79f38f4081b4f5"
        )
    ]
)
