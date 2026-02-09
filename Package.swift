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
            url: "https://github.com/f1uke/DataTaggingKMP/releases/download/1.0.5/DataTaggingKMP.xcframework.zip",
            checksum: "bd03f1b1369e58b68b8d866b3b08efaca51d958a69d77b0a522922b956862571"
        )
    ]
)
