package com.example.snapsolvesudoku.camera

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.util.Size

class CamObject {

    lateinit var cameraId : String
    lateinit var viewFinderSize : Size

    val REAR_FACING = CameraCharacteristics.LENS_FACING_BACK

}