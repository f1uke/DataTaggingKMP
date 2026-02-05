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
            url: "https://github.com/f1uke/DataTaggingKMP/releases/download/1.0.0/DataTaggingKMP.xcframework.zip",
            checksum: "8929853cd0324ad77a755720a5124a13d5dbd9448e98fca44717364d5dff795b"
        )
    ]
)
