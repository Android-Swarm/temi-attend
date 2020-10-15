package com.zetzaus.temiattend.temperature

import com.ogawa.temiirsdk.IrDataUtil
import com.ogawa.temiirsdk.IrSensorUtil

/**
 * Wrapper class for EEM or FRM data.
 *
 * @property body The body of the response. In other words, the raw data with the header dropped.
 */
sealed class TmIrResponse(val body: String) {

    abstract val data: IntArray

    /** Wrapper class for FRM. */
    class FRM(body: String) : TmIrResponse(body) {
        override val data = IrDataUtil.getFRMData(body) ?: intArrayOf()

        val distance = IrDataUtil.getDistanceData(body) ?: 0

        val frameNo = IrDataUtil.getFrmNo(data) ?: -1
    }

    /** Wrapper class for EEM. */
    class EEM(body: String) : TmIrResponse(body) {
        override val data = IrDataUtil.getEEMData(body) ?: intArrayOf()

        val correction = IrSensorUtil.mlx90640ExtractParameters(data) ?: -1
    }
}
