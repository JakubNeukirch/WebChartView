# WebChartView
WebChart for android

![Screenshoot](https://github.com/JakubNeukirch/WebChartView/blob/master/Screenshot_1538063220.png)

## Installation
Add Repository <br/>
```maven { url 'https://jitpack.io' }```<br/>
Add Dependency<br/>
```implementation 'com.github.JakubNeukirch:WebChartView:0.5.0'```<br/>

## Usage
All you need to do is add list of `Point`s into your webchart, like this:
```kotlin
webChart.points = listOf(
                Point(0, 3, 6, ""),
                Point(1, 5, 7, "Oar"),
                Point(2, 1, 4,"HIfddd"),
                Point(3, 3, 10, "Test"),
                Point(4, 3, 15, "I text")
        )
```
`Point` constructor
|name|type|description|
|---|:---:|:---:|
|value|Int|value out of maxValue|
|maxValue|Int|max value|
|caption|String|The label which will be displayed above|
