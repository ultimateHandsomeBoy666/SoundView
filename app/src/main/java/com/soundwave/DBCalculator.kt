package com.soundwave

class DBCalculator {

    fun calculateDB(pcmData: ShortArray, readSize: Int): Double {
        val rms = calculateRMS(pcmData, readSize)
        val db = rmsToDb(rms)
        return db
    }

    private fun calculateRMS(pcmData: ShortArray, readSize: Int): Double {
        if (readSize == 0) {
            return 0.0
        }
        // 使用 Double 避免平方后溢出
        var sumOfSquares = 0.0
        for (i in 0 until readSize) {
            val sample = pcmData[i].toDouble()
            sumOfSquares += sample * sample
        }
        val meanSquare = sumOfSquares / readSize
        return Math.sqrt(meanSquare)
    }

    private fun rmsToDb(rms: Double, maxAmplitude: Double = 32767.0): Double {
        if (rms == 0.0) {
            // 返回一个非常小的值代表静音，避免 log10(0) 导致 -Infinity
            return -96.0
        }
        // 20 * log10(rms / maxAmplitude)
        val db = 20 * Math.log10(rms / maxAmplitude)
        // 平滑处理示例
        val alpha = 0.2 // 平滑系数，越小越平滑
        return (alpha * db + (1.0 - alpha) * db) + 40
    }
}