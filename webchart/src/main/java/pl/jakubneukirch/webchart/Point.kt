package pl.jakubneukirch.webchart

data class Point(
        var armIndex: Int,
        var value: Int,
        var maxValue: Int,
        var caption: String = ""
)