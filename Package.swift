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
            url: "https://github.com/finnomena/DataTaggingKMP/releases/download/1.0.9/DataTaggingKMP.xcframework.zip",
            checksum: "c930dbe2a42f6fe85ab1a091f6faa005aa47f019c99026ece2af6169348301ba"
        )
    ]
)
