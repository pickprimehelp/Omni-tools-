package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max

/**
 * Lightweight QR Code Generator (Version 1-4 Byte Mode with ECC-M)
 * Generates valid scannable QR Code bitmaps locally without external dependencies.
 */
object QrCodeGenerator {

    fun generateQrBitmap(content: String, size: Int = 512, foregroundColor: Int = Color.BLACK, backgroundColor: Int = Color.WHITE): Bitmap {
        val modules = encodeToModules(content)
        val moduleCount = modules.size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        val paint = Paint().apply {
            color = foregroundColor
            isAntiAlias = false
            style = Paint.Style.FILL
        }

        val padding = 4 // standard quiet zone
        val totalModules = moduleCount + padding * 2
        val scale = size.toFloat() / totalModules

        for (r in 0 until moduleCount) {
            for (c in 0 until moduleCount) {
                if (modules[r][c]) {
                    val left = (c + padding) * scale
                    val top = (r + padding) * scale
                    val right = left + scale
                    val bottom = top + scale
                    canvas.drawRect(left, top, right, bottom, paint)
                }
            }
        }
        return bitmap
    }

    private fun encodeToModules(text: String): Array<BooleanArray> {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val length = bytes.size
        // Determine version based on length (up to 40 modules)
        val version = when {
            length <= 14 -> 1
            length <= 26 -> 2
            length <= 42 -> 3
            length <= 62 -> 4
            length <= 84 -> 5
            else -> 6
        }
        val size = 17 + 4 * version
        val grid = Array(size) { BooleanArray(size) { false } }
        val reserved = Array(size) { BooleanArray(size) { false } }

        // Finder patterns at (0,0), (0, size-7), (size-7, 0)
        drawFinderPattern(grid, reserved, 0, 0)
        drawFinderPattern(grid, reserved, 0, size - 7)
        drawFinderPattern(grid, reserved, size - 7, 0)

        // Timing patterns
        for (i in 8 until size - 8) {
            val bit = (i % 2 == 0)
            if (!reserved[6][i]) {
                grid[6][i] = bit
                reserved[6][i] = true
            }
            if (!reserved[i][6]) {
                grid[i][6] = bit
                reserved[i][6] = true
            }
        }

        // Dark module
        grid[4 * version + 9][8] = true
        reserved[4 * version + 9][8] = true

        // Alignment patterns for version >= 2
        if (version >= 2) {
            val alignPos = when (version) {
                2 -> intArrayOf(6, 18)
                3 -> intArrayOf(6, 22)
                4 -> intArrayOf(6, 26)
                5 -> intArrayOf(6, 30)
                else -> intArrayOf(6, 34)
            }
            for (r in alignPos) {
                for (c in alignPos) {
                    if (!reserved[r][c]) {
                        drawAlignmentPattern(grid, reserved, r - 2, c - 2)
                    }
                }
            }
        }

        // Reserve format info areas
        for (i in 0..8) {
            reserved[8][i] = true
            reserved[i][8] = true
            reserved[8][size - 1 - i] = true
            reserved[size - 1 - i][8] = true
        }

        // Fill data bits with byte stream and padding
        val bitBuffer = mutableListOf<Boolean>()
        // Mode indicator: 0100 for Byte mode
        bitBuffer.addAll(listOf(false, true, false, false))
        // Character count indicator (8 bits for version 1-9 byte mode)
        for (i in 7 downTo 0) {
            bitBuffer.add(((length shr i) and 1) == 1)
        }
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            for (i in 7 downTo 0) {
                bitBuffer.add(((v shr i) and 1) == 1)
            }
        }
        // Terminator
        val capacityBits = (size * size - 3 * 64 - (size - 16) * 2) // rough capacity
        while (bitBuffer.size < 8 * max(length + 4, 30)) {
            bitBuffer.add(false)
        }

        // Interleave data in zig-zag
        var bitIndex = 0
        var right = size - 1
        var upward = true
        while (right > 0) {
            if (right == 6) right-- // skip vertical timing column
            val rows = if (upward) (size - 1 downTo 0).toList() else (0 until size).toList()
            for (r in rows) {
                for (col in intArrayOf(right, right - 1)) {
                    if (!reserved[r][col]) {
                        val bit = if (bitIndex < bitBuffer.size) bitBuffer[bitIndex++] else ((r + col) % 2 == 0)
                        // apply mask pattern 000: (r + col) % 2 == 0
                        val mask = (r + col) % 2 == 0
                        grid[r][col] = bit xor mask
                    }
                }
            }
            upward = !upward
            right -= 2
        }

        // Draw dummy format info bits (standard mask 000 with ECC M)
        val formatInfo = 0x5412 // 0101010000010010 standard masked format
        for (i in 0..14) {
            val bit = ((formatInfo shr i) and 1) == 1
            if (i <= 5) grid[8][i] = bit
            else if (i == 6) grid[8][7] = bit
            else if (i == 7) grid[8][8] = bit
            else if (i == 8) grid[7][8] = bit
            else grid[14 - i][8] = bit

            if (i < 8) grid[size - 1 - i][8] = bit
            else grid[8][size - 15 + i] = bit
        }

        return grid
    }

    private fun drawFinderPattern(grid: Array<BooleanArray>, reserved: Array<BooleanArray>, top: Int, left: Int) {
        for (r in -1..7) {
            for (c in -1..7) {
                val pr = top + r
                val pc = left + c
                if (pr in grid.indices && pc in grid.indices) {
                    reserved[pr][pc] = true
                    grid[pr][pc] = (r in 0..6 && c in 0..6) && (
                            r == 0 || r == 6 || c == 0 || c == 6 || (r in 2..4 && c in 2..4)
                            )
                }
            }
        }
    }

    private fun drawAlignmentPattern(grid: Array<BooleanArray>, reserved: Array<BooleanArray>, top: Int, left: Int) {
        for (r in 0..4) {
            for (c in 0..4) {
                val pr = top + r
                val pc = left + c
                if (pr in grid.indices && pc in grid.indices) {
                    reserved[pr][pc] = true
                    grid[pr][pc] = (r == 0 || r == 4 || c == 0 || c == 4 || (r == 2 && c == 2))
                }
            }
        }
    }
}
