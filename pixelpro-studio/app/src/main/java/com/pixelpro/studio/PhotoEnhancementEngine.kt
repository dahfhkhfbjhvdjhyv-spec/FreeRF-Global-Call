package com.pixelpro.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Multi-stage classical photo enhancement engine (no AI / no network).
 *
 * Pipeline:
 *  1) high-quality resampling
 *  2) edge-aware bilateral denoise (protects strong edges better than median blur)
 *  3) luminance-only multi-scale detail and local-contrast enhancement
 *  4) thresholded, halo-limited sharpening
 *  5) restrained color/contrast finishing
 *
 * This improves captured information; it cannot recreate details absent from the source.
 */
object PhotoEnhancementEngine {
    data class Settings(
        val outputLongEdge: Int = 1920,
        val denoise: Int = 1,             // 0..2 edge-aware passes
        val sharpen: Float = 0.72f,       // 0..1.5
        val contrast: Float = 1.06f,      // 0.8..1.3
        val saturation: Float = 1.04f,    // 0.7..1.3
        val detail: Float = 0.28f,        // 0..0.8 fine-detail recovery
        val localContrast: Float = 0.18f, // 0..0.5
        val edgeProtection: Float = 0.72f // 0..1
    )

    fun enhance(source: Bitmap, settings: Settings = Settings()): Bitmap {
        require(source.width > 0 && source.height > 0) { "Source image is empty" }
        val targetEdge = settings.outputLongEdge.coerceIn(256, 4096)
        val scale = targetEdge.toFloat() / max(source.width, source.height).toFloat()
        val outW = max(1, (source.width * scale).roundToInt())
        val outH = max(1, (source.height * scale).roundToInt())
        var work = Bitmap.createScaledBitmap(source, outW, outH, true)

        repeat(settings.denoise.coerceIn(0, 2)) {
            val next = bilateralDenoise(work, settings.edgeProtection.coerceIn(0f, 1f))
            work.recycle()
            work = next
        }

        val enhanced = enhanceLuminance(
            work,
            settings.sharpen.coerceIn(0f, 1.5f),
            settings.detail.coerceIn(0f, 0.8f),
            settings.localContrast.coerceIn(0f, 0.5f),
            settings.edgeProtection.coerceIn(0f, 1f)
        )
        work.recycle()
        val finished = finishColor(
            enhanced,
            settings.contrast.coerceIn(0.8f, 1.3f),
            settings.saturation.coerceIn(0.7f, 1.3f)
        )
        enhanced.recycle()
        return finished
    }

    /** 3x3 bilateral filter: spatial and luminance similarity both affect the weight. */
    private fun bilateralDenoise(input: Bitmap, edgeProtection: Float): Bitmap {
        val w = input.width
        val h = input.height
        val src = IntArray(w * h)
        val dst = IntArray(w * h)
        input.getPixels(src, 0, w, 0, 0, w, h)
        val spatial = floatArrayOf(0.3679f, 0.6065f, 0.3679f, 0.6065f, 1f, 0.6065f, 0.3679f, 0.6065f, 0.3679f)
        val rangeSigma = 18f + edgeProtection * 30f
        val rangeDenom = 2f * rangeSigma * rangeSigma

        for (y in 0 until h) for (x in 0 until w) {
            val index = y * w + x
            val center = src[index]
            val centerY = luma(center)
            var sumW = 0f
            var sumR = 0f; var sumG = 0f; var sumB = 0f
            var k = 0
            for (dy in -1..1) for (dx in -1..1) {
                val sx = (x + dx).coerceIn(0, w - 1)
                val sy = (y + dy).coerceIn(0, h - 1)
                val c = src[sy * w + sx]
                val difference = luma(c) - centerY
                val rangeWeight = exp(-(difference * difference) / rangeDenom)
                val weight = spatial[k++] * rangeWeight
                sumW += weight
                sumR += Color.red(c) * weight
                sumG += Color.green(c) * weight
                sumB += Color.blue(c) * weight
            }
            dst[index] = Color.argb(
                Color.alpha(center),
                (sumR / sumW).roundToInt().coerceIn(0, 255),
                (sumG / sumW).roundToInt().coerceIn(0, 255),
                (sumB / sumW).roundToInt().coerceIn(0, 255)
            )
        }
        return Bitmap.createBitmap(dst, w, h, Bitmap.Config.ARGB_8888)
    }

    /**
     * Enhances luminance instead of sharpening each RGB channel independently.
     * This reduces colored fringes. A soft threshold suppresses noise amplification;
     * edge protection limits enhancement around strong transitions.
     */
    private fun enhanceLuminance(
        input: Bitmap,
        sharpen: Float,
        detail: Float,
        localContrast: Float,
        edgeProtection: Float
    ): Bitmap {
        val w = input.width
        val h = input.height
        val src = IntArray(w * h)
        val dst = IntArray(w * h)
        input.getPixels(src, 0, w, 0, 0, w, h)
        val yPlane = FloatArray(src.size) { luma(src[it]) }
        val kernel = intArrayOf(1, 2, 1, 2, 4, 2, 1, 2, 1)

        for (y in 0 until h) for (x in 0 until w) {
            val i = y * w + x
            var blur = 0f
            var k = 0
            var minY = 255f
            var maxY = 0f
            for (dy in -1..1) for (dx in -1..1) {
                val sx = (x + dx).coerceIn(0, w - 1)
                val sy = (y + dy).coerceIn(0, h - 1)
                val value = yPlane[sy * w + sx]
                blur += value * kernel[k++]
                minY = min(minY, value)
                maxY = max(maxY, value)
            }
            blur /= 16f
            val originalY = yPlane[i]
            val highFrequency = originalY - blur
            val threshold = 2.5f + edgeProtection * 4f
            val cleanDetail = if (abs(highFrequency) <= threshold) 0f else
                highFrequency - (if (highFrequency > 0f) threshold else -threshold)

            val edgeStrength = ((maxY - minY) / 48f).coerceIn(0f, 1f)
            val protect = 1f - edgeProtection * edgeStrength * 0.45f
            val sharpenDelta = cleanDetail * sharpen * protect
            val detailDelta = highFrequency * detail * protect

            // 5x5 neighborhood mean gives a gentle local-contrast reference.
            var localSum = 0f
            var count = 0
            for (dy in -2..2) for (dx in -2..2) {
                val sx = (x + dx).coerceIn(0, w - 1)
                val sy = (y + dy).coerceIn(0, h - 1)
                localSum += yPlane[sy * w + sx]
                count++
            }
            val localMean = localSum / count
            val localDelta = (originalY - localMean) * localContrast * protect
            val targetY = (originalY + sharpenDelta + detailDelta + localDelta).coerceIn(0f, 255f)

            val c = src[i]
            val delta = targetY - originalY
            dst[i] = Color.argb(
                Color.alpha(c),
                (Color.red(c) + delta).roundToInt().coerceIn(0, 255),
                (Color.green(c) + delta).roundToInt().coerceIn(0, 255),
                (Color.blue(c) + delta).roundToInt().coerceIn(0, 255)
            )
        }
        return Bitmap.createBitmap(dst, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun finishColor(input: Bitmap, contrast: Float, saturation: Float): Bitmap {
        val w = input.width
        val h = input.height
        val src = IntArray(w * h)
        val dst = IntArray(w * h)
        input.getPixels(src, 0, w, 0, 0, w, h)
        for (i in src.indices) {
            val c = src[i]
            val r0 = Color.red(c).toFloat()
            val g0 = Color.green(c).toFloat()
            val b0 = Color.blue(c).toFloat()
            val gray = 0.299f * r0 + 0.587f * g0 + 0.114f * b0
            val r = ((gray + (r0 - gray) * saturation - 128f) * contrast + 128f)
            val g = ((gray + (g0 - gray) * saturation - 128f) * contrast + 128f)
            val b = ((gray + (b0 - gray) * saturation - 128f) * contrast + 128f)
            dst[i] = Color.argb(Color.alpha(c), clamp(r), clamp(g), clamp(b))
        }
        return Bitmap.createBitmap(dst, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun luma(color: Int): Float =
        0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color)

    private fun clamp(value: Float): Int = value.roundToInt().coerceIn(0, 255)
}
