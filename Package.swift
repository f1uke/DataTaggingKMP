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
            url: "https://github.com/f1uke/DataTaggingKMP/releases/download/1.0.1/DataTaggingKMP.xcframework.zip",
            checksum: "b49385619fdb42e524f1df2dd5716bfb08216542ab883b917d31a1bc7f16a4c8"
        )
    ]
)
