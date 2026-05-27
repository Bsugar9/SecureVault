package com.example.securedb

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.*

class AnimationView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint()
    private val random = Random()
    private var animationType = "None"
    
    // Fireworks state
    private val particles = mutableListOf<Particle>()
    
    // Matrix state
    private var columns = 0
    private var columnOffsets = FloatArray(0)
    private var columnSpeeds = FloatArray(0)
    private val charSize = 45f
    private val matrixChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZﾊﾐﾋｰｳｼﾅﾓﾆｻﾜﾂｵﾘｱﾎﾃﾏｹﾒｴｶｷﾑﾕﾗｾﾈｽﾀﾇﾍ".toCharArray()

    // Lightning state
    private var lightningPath = Path()
    private var flashAlpha = 0
    private var strikeCounter = 0

    // Starfield state
    private val stars = mutableListOf<Star>()
    private val starCount = 300 // Doubled from 150
    private val maxDepth = 1000f

    // Lava state
    private var lavaHeight = 0f
    private var lavaPhase = 0 
    private var hardenProgress = 0f
    private val lavaParticles = mutableListOf<Particle>()

    // Tornado state
    private val tornadoParticles = mutableListOf<TornadoParticle>()
    private var tornadoX = 0f
    private var tornadoTargetX = 0f
    private var tornadoBaseWidth = 100f
    private var tornadoAngle = 0f

    // Wormhole state
    private val wormholeRings = mutableListOf<WormholeRing>()
    private var wormholeAngle = 0f

    fun setAnimation(type: String) {
        animationType = type
        particles.clear()
        lightningPath.reset()
        stars.clear()
        lavaParticles.clear()
        tornadoParticles.clear()
        wormholeRings.clear()
        
        flashAlpha = 0
        
        when (type) {
            "Digital Rain" -> setupMatrix()
            "Starfield" -> setupStarfield()
            "Tornado" -> setupTornado()
            "Wormhole" -> setupWormhole()
        }
        invalidate()
    }

    private fun setupMatrix() {
        if (width > 0) {
            columns = (width / charSize).toInt() + 1
            columnOffsets = FloatArray(columns) { random.nextFloat() * -20f }
            columnSpeeds = FloatArray(columns) { random.nextFloat() * 0.5f + 0.2f }
        }
    }

    private fun setupStarfield() {
        if (width > 0) {
            for (i in 0 until starCount) {
                stars.add(Star(
                    (random.nextFloat() - 0.5f) * width * 2,
                    (random.nextFloat() - 0.5f) * height * 2,
                    random.nextFloat() * maxDepth
                ))
            }
        }
    }

    private fun setupTornado() {
        tornadoX = width / 2f
        tornadoTargetX = tornadoX
        for (i in 0 until 400) {
            tornadoParticles.add(TornadoParticle(random))
        }
    }

    private fun setupWormhole() {
        if (width > 0) {
            for (i in 0 until 40) {
                wormholeRings.add(WormholeRing(i * 25f, random))
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        when (animationType) {
            "Digital Rain" -> setupMatrix()
            "Starfield" -> setupStarfield()
            "Tornado" -> setupTornado()
            "Wormhole" -> setupWormhole()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        when (animationType) {
            "Fireworks" -> drawFireworks(canvas)
            "Digital Rain" -> drawMatrix(canvas)
            "Bolt Lightning" -> drawLightning(canvas)
            "Starfield" -> drawStarfield(canvas)
            "Lava" -> drawLava(canvas)
            "Tornado" -> drawTornado(canvas)
            "Wormhole" -> drawWormhole(canvas)
        }
        
        if (animationType != "None") {
            postInvalidateDelayed(30)
        }
    }

    private fun drawFireworks(canvas: Canvas) {
        if (random.nextInt(15) == 0) {
            val cx = random.nextFloat() * width
            val cy = random.nextFloat() * height
            val color = Color.HSVToColor(floatArrayOf(random.nextFloat() * 360f, 0.8f, 1f))
            for (i in 0..50) {
                particles.add(Particle(cx, cy, color, random))
            }
        }
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.alpha <= 0) {
                iterator.remove()
                continue
            }
            paint.color = p.color
            paint.alpha = p.alpha
            canvas.drawCircle(p.x, p.y, p.size, paint)
            p.update()
        }
    }

    private fun drawMatrix(canvas: Canvas) {
        paint.textSize = charSize
        paint.textAlign = Paint.Align.CENTER
        for (i in 0 until columns) {
            val x = i * charSize + charSize / 2
            val currentYIndex = columnOffsets[i]
            for (j in 0..15) {
                val yIndex = currentYIndex - j
                if (yIndex < 0) continue
                val y = yIndex * charSize
                if (y > height + charSize) continue
                if (j == 0) {
                    paint.color = Color.parseColor("#E0FFE0")
                    paint.setShadowLayer(10f, 0f, 0f, Color.parseColor("#00FF41"))
                } else {
                    val alpha = (255 * (1 - j / 15f)).toInt().coerceIn(0, 255)
                    paint.color = Color.parseColor("#00FF41")
                    paint.alpha = alpha
                    paint.clearShadowLayer()
                }
                val char = matrixChars[random.nextInt(matrixChars.size)]
                canvas.drawText(char.toString(), x, y, paint)
            }
            columnOffsets[i] += columnSpeeds[i]
            if (columnOffsets[i] * charSize > height + (charSize * 15)) {
                columnOffsets[i] = -random.nextInt(10).toFloat()
                columnSpeeds[i] = random.nextFloat() * 0.5f + 0.2f
            }
        }
        paint.clearShadowLayer()
    }

    private fun drawLightning(canvas: Canvas) {
        if (flashAlpha > 0) {
            paint.color = Color.WHITE
            paint.alpha = flashAlpha
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            flashAlpha -= 40
        }
        if (strikeCounter > 0) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 8f
            paint.color = Color.parseColor("#A0CCFF")
            paint.setShadowLayer(20f, 0f, 0f, Color.WHITE)
            canvas.drawPath(lightningPath, paint)
            strikeCounter--
        } else {
            if (random.nextInt(40) == 0) {
                generateLightningPath()
                strikeCounter = 3 + random.nextInt(5)
                flashAlpha = 150
            }
        }
        paint.style = Paint.Style.FILL
        paint.clearShadowLayer()
    }

    private fun drawStarfield(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f // Thicker lines for Hyperdrive
        for (star in stars) {
            val px = (star.x / star.z) * 100f + centerX
            val py = (star.y / star.z) * 100f + centerY
            
            star.z -= 25f // Faster speed
            
            if (star.z < maxDepth) {
                val nextPx = (star.x / star.z) * 100f + centerX
                val nextPy = (star.y / star.z) * 100f + centerY
                
                val brightness = (255 * (1 - star.z / maxDepth)).toInt().coerceIn(0, 255)
                paint.setARGB(brightness, 255, 255, 255)
                
                canvas.drawLine(px, py, nextPx, nextPy, paint)
            }
            
            if (star.z <= 10f) {
                star.z = maxDepth
                star.x = (random.nextFloat() - 0.5f) * width * 2
                star.y = (random.nextFloat() - 0.5f) * height * 2
            }
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawLava(canvas: Canvas) {
        when (lavaPhase) {
            0 -> {
                lavaHeight += 4f
                val lavaColor = Color.parseColor("#FF4500")
                paint.color = lavaColor
                canvas.drawRect(0f, height - lavaHeight, width.toFloat(), height.toFloat(), paint)
                if (random.nextInt(5) == 0) {
                    lavaParticles.add(Particle(random.nextFloat() * width, height - lavaHeight, lavaColor, random).apply { 
                        vy = -random.nextFloat() * 5f
                        vx = (random.nextFloat() - 0.5f) * 2f
                    })
                }
                if (lavaHeight >= height) lavaPhase = 1
            }
            1 -> {
                hardenProgress += 0.01f
                val orange = Color.parseColor("#FF4500")
                val charcoal = Color.parseColor("#2C2C2C")
                paint.color = interpolateColor(orange, charcoal, hardenProgress)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                if (hardenProgress >= 1.0f) {
                    lavaPhase = 2
                    for (i in 0..100) {
                        lavaParticles.add(Particle(width / 2f, height / 2f, Color.parseColor("#FF4500"), random).apply {
                            vx = (random.nextFloat() - 0.5f) * 40f
                            vy = (random.nextFloat() - 0.5f) * 40f
                            alpha = 255
                        })
                    }
                }
            }
            2 -> {
                if (lavaParticles.isEmpty()) {
                    lavaPhase = 0
                    lavaHeight = 0f
                    hardenProgress = 0f
                }
            }
        }
        val iterator = lavaParticles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.alpha <= 0) {
                iterator.remove()
                continue
            }
            paint.color = p.color
            paint.alpha = p.alpha
            canvas.drawCircle(p.x, p.y, p.size * 2, paint)
            p.update()
        }
    }

    private fun drawTornado(canvas: Canvas) {
        if (abs(tornadoX - tornadoTargetX) < 10f) {
            tornadoTargetX = random.nextFloat() * width
        }
        tornadoX += (tornadoTargetX - tornadoX) * 0.02f
        tornadoAngle += 0.1f
        tornadoBaseWidth = 150f + (sin(tornadoAngle) * 50f)
        tornadoParticles.sortBy { it.z }
        for (p in tornadoParticles) {
            val yFactor = 1f - (p.y / height)
            val currentRadius = tornadoBaseWidth * (1f + yFactor * 4f)
            val rotationX = sin(p.angle + tornadoAngle * p.speedMult) * currentRadius
            val drawX = tornadoX + rotationX
            val drawY = p.y
            val pSize = (p.baseSize * (1f + p.z)).coerceAtLeast(2f)
            val alpha = (150 + p.z * 100).toInt().coerceIn(0, 255)
            paint.color = p.color
            paint.alpha = alpha
            canvas.drawRect(drawX, drawY, drawX + pSize, drawY + pSize, paint)
            p.update(height)
        }
    }

    private fun drawWormhole(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        wormholeAngle += 0.05f
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        
        for (ring in wormholeRings) {
            ring.z += 8f
            if (ring.z > 1000f) {
                ring.z = 0f
                ring.hue = random.nextFloat() * 360f
            }
            
            // Expand size to cover entire screen (using max dimension)
            val screenSize = max(width, height).toFloat()
            val size = (ring.z / 1000f).pow(1.5f) * screenSize * 1.5f
            val alpha = (ring.z / 1000f * 255).toInt().coerceIn(0, 255)
            
            val offsetX = sin(wormholeAngle + ring.z * 0.01f) * 80f
            val offsetY = cos(wormholeAngle + ring.z * 0.01f) * 80f
            
            paint.color = Color.HSVToColor(alpha, floatArrayOf(ring.hue, 0.8f, 1f))
            
            val path = Path()
            val sides = 8
            for (i in 0 until sides) {
                val angle = (i * 2 * PI / sides).toFloat()
                val rx = centerX + (size/2) * cos(angle) + offsetX
                val ry = centerY + (size/2) * sin(angle) + offsetY
                if (i == 0) path.moveTo(rx, ry) else path.lineTo(rx, ry)
            }
            path.close()
            canvas.drawPath(path, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun interpolateColor(a: Int, b: Int, proportion: Float): Int {
        val hsva = FloatArray(3)
        val hsvb = FloatArray(3)
        Color.colorToHSV(a, hsva)
        Color.colorToHSV(b, hsvb)
        for (i in 0..2) {
            hsvb[i] = hsva[i] + (hsvb[i] - hsva[i]) * proportion
        }
        return Color.HSVToColor(hsvb)
    }

    private fun generateLightningPath() {
        lightningPath.reset()
        var curX = random.nextFloat() * width
        var curY = 0f
        lightningPath.moveTo(curX, curY)
        while (curY < height) {
            curX += (random.nextFloat() - 0.5f) * 150f
            curY += random.nextFloat() * 100f
            lightningPath.lineTo(curX, curY)
            if (random.nextInt(5) == 0) {
                drawBranch(curX, curY)
            }
        }
    }

    private fun drawBranch(x: Float, y: Float) {
        var bx = x
        var by = y
        val branchPoints = 3 + random.nextInt(5)
        for (i in 0 until branchPoints) {
            bx += (random.nextFloat() - 0.5f) * 100f
            by += random.nextFloat() * 80f
            lightningPath.moveTo(x, y)
            lightningPath.lineTo(bx, by)
        }
    }

    private class Particle(var x: Float, var y: Float, val color: Int, random: Random) {
        var vx = (random.nextFloat() - 0.5f) * 15f
        var vy = (random.nextFloat() - 0.5f) * 15f
        var alpha = 255
        val size = random.nextFloat() * 5f + 2f
        fun update() {
            x += vx
            y += vy
            vy += 0.2f
            alpha -= 5
        }
    }
    
    private class Star(var x: Float, var y: Float, var z: Float)

    private class TornadoParticle(random: Random) {
        var y = random.nextFloat() * 2000f 
        var angle = random.nextFloat() * (PI.toFloat() * 2f)
        var z = random.nextFloat() * 2f - 1f 
        var speedMult = 0.5f + random.nextFloat() * 1.5f
        var baseSize = 4f + random.nextFloat() * 6f
        val color = if (random.nextBoolean()) Color.GRAY else Color.parseColor("#555555")
        fun update(height: Int) {
            y -= 5f * speedMult 
            angle += 0.1f * speedMult
            if (y < 0) y = height.toFloat()
        }
    }

    private class WormholeRing(var z: Float, random: Random) {
        var hue = random.nextFloat() * 360f
    }
}
