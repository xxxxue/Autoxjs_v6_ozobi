package org.autojs.autoxjs.ui.compose.util

import androidx.compose.runtime.Composable

@Composable
fun getFitRandomColor(nightMode: Boolean):Int{
    var hue = (Math.random() * 360).toFloat()
    while(hue > 30 && hue < 210){
        hue = (Math.random() * 360).toFloat()
    }
    val bright = 0.8f
    val saturation = (Math.random()*0.2 + 0.8).toFloat()
    var alpha = 230
    if(nightMode){
        alpha = 180
//        while(!(hue > 30 && hue < 210)){
//            hue = (Math.random() * 360).toFloat()
//        }
//        bright = 1f
//        saturation = (Math.random()*0.4 + 0.2).toFloat()
    }
    val fArr = FloatArray(3)
    fArr[0] = hue
    fArr[1] = saturation
    fArr[2] = bright
    return android.graphics.Color.HSVToColor(alpha, fArr)
}