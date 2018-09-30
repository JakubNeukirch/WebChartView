[![](https://jitpack.io/v/JakubNeukirch/WebChartView.svg)](https://jitpack.io/#JakubNeukirch/WebChartView)
# WebChartView
WebChart for android

![Screenshot](https://github.com/JakubNeukirch/WebChartView/blob/master/newappearance.png)

## Installation
Add Repository <br/>
```maven { url 'https://jitpack.io' }```<br/>
Add Dependency<br/>
```implementation 'com.github.JakubNeukirch:WebChartView:0.5.2'```<br/>

## Usage
All you need to do is add list of `Point`s into your web chart, like this:
```kotlin
webChart.points = listOf(
                Point(3, 6, "PPP"),
                Point(5, 7, "Oar"),
                Point(1, 4,"HIfddd"),
                Point(3, 10, "Test"),
                Point(3, 15, "I text")
        )
```
`Point` constructor

| name | type | description |
| - | - | - |
| value | Int | value out of maxValue |
| maxValue | Int | max value |
| caption | String | The label which will be displayed above |

Available xml attributes

| name | type | description|
| - | - | - |
| labelTextSize | dimension | Size of label above web |
| webColor | color | Color of values web |


## Changelog

### 0.5.2
- Improved appearance