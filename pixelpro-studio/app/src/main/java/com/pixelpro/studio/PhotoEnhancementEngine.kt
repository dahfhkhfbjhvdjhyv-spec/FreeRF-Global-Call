package com.pixelpro.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Classical, non-AI image enhancement.
 * Operations: edge-preserving-ish median denoise, unsharp masking,
 * local contrast, restrained saturation, and high quality resampling.
 * It cannot reconstruct detail that was never captured by the camera.
 */
object PhotoEnhancementEngine {
    data class Settings(
        val outputLongEdge: Int = 1920,
        val denoise: Int = 1,          // 0..2 median passes
        val sharpen: Float = 0.72f,    // 0..1.5
        val contrast: Float = 1.06f,   // 0.8..1.3
        val saturation: Float = 1.04f  // 0.7..1.3
    )

    fun enhance(source: Bitmap, settings: Settings = Settings()): Bitmap {
        val maxEdge = max(source.width, source.height)
        val scale = settings.outputLongEdge.toFloat() / maxEdge.toFloat()
        val outW = max(1, (source.width * scale).roundToInt())
        val outH = max(1, (source.height * scale).roundToInt())
        var work = Bitmap.createScaledBitmap(source, outW, outH, true)
        repeat(settings.denoise.coerceIn(0, 2)) {
            val next = medianDenoise(work)
            if (work !== source) work.recycle()
            work = next
        }
        val sharpened = unsharpMask(work, settings.sharpen.coerceIn(0f, 1.5f))
        if (work !== source) work.recycle()
        val tuned = tuneColor(sharpened, settings.contrast, settings.saturation)
        sharpened.recycle()
        return tuned
    }

    private fun medianDenoise(input: Bitmap): Bitmap {
        val w = input.width
        val h = input.height
        val src = IntArray(w * h)
        val dst = IntArray(w * h)
        input.getPixels(src, 0, w, 0, 0, w, h)
        val rs = IntArray(9); val gs = IntArray(9); val bs = IntArray(9)
        for (y in 0 until h) for (x in 0 until w) {
            val idx = y * w + x
            if (x == 0 || y == 0 || x == w - 1 || y == h - 1) {
                dst[idx] = src[idx]; continue
            }
            var n = 0
            for (dy in -1..1) for (dx in -1..1) {
                val c = src[(y + dy) * w + x + dx]
                rs[n] = Color.red(c); gs[n] = Color.green(c); bs[n] = Color.blue(c); n++
            }
            rs.sort(); gs.sort(); bs.sort()
            val a = Color.alpha(src[idx])
            dst[idx] = Color.argb(a, rs[4], gs[4], bs[4])
        }
        return Bitmap.createBitmap(dst, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun unsharpMask(input: Bitmap, amount: Float): Bitmap {
        if (amount <= 0f) return input.copy(Bitmap.Config.ARGB_8888, false)
        val w = input.width; val h = input.height
        val src = IntArray(w * h); val dst = IntArray(w * h)
        input.getPixels(src, 0, w, 0, 0, w, h)
        val weights = intArrayOf(1, 2, 1, 2, 4, 2, 1, 2, 1)
        for (y in 0 until h) for (x in 0 until w) {
            val idx = y * w + x
            var rr = 0; var gg = 0; var bb = 0; var k = 0
            for (dy in -1..1) for (dx in -1..1) {
                val sx = (x + dx).coerceIn(0, w - 1)
                val sy = (y + dy).coerceIn(0, h - 1)
                val c = src[sy * w + sx]; val wt = weights[k++]
                rr += Color.red(c) * wt; gg += Color.green(c) * wt; bb += Color.blue(c) * wt
            }
            val c = src[idx]
            val blurR = rr / 16f; val blurG = gg / 16f; val blurB = bb / 16f
            dst[idx] = Color.argb(
                Color.alpha(c),
                clamp(Color.red(c) + amount * (Color.red(c) - blurR)),
                clamp(Color.green(c) + amount * (Color.green(c) - blurG)),
                clamp(Color.blue(c) + amount * (Color.blue(c) - blurB))
            )
        }
        return Bitmap.createBitmap(dst, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun tuneColor(input: Bitmap, contrast: Float, saturation: Float): Bitmap {
        val w = input.width; val h = input.height
        val src = IntArray(w * h); val dst = IntArray(w * h)
        input.getPixels(src, 0, w, 0, 0, w, h)
        val cFactor = contrast.coerceIn(0.8f, 1.3f)
        val sFactor = saturation.coerceIn(0.7f, 1.3f)
        for (i in src.indices) {
            val c = src[i]
            val r0 = Color.red(c).toFloat(); val g0 = Color.green(c).toFloat(); val b0 = Color.blue(c).toFloat()
            val gray = 0.299f * r0 + 0.587f * g0 + 0.114f * b0
            val r = ((gray + (r0 - gray) * sFactor - 128f) * cFactor + 128f)
            val g = ((gray + (g0 - gray) * sFactor - 128f) * cFactor + 128f)
            val b = ((gray + (b0 - gray) * sFactor - 128f) * cFactor + 128f)
            dst[i] = Color.argb(Color.alpha(c), clamp(r), clamp(g), clamp(b))
        }
        return Bitmap.createBitmap(dst, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun clamp(v: Float): Int = v.roundToInt().coerceIn(0, 255)
}
