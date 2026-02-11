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
            url: "https://github.com/f1uke/DataTaggingKMP/releases/download/1.0.7/DataTaggingKMP.xcframework.zip",
            checksum: "d1cf65572e9f8936e30f34bd3ed262c9f0b5fd055c44324e85c2eb4acaf9a550"
        )
    ]
)
