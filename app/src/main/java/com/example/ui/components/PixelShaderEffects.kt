package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import org.intellij.lang.annotations.Language

@Language("AGSL")
private const val GLITCH_SCAN_AGSL = """
uniform shader contents;
uniform float2 uResolution;
uniform float uTime;
uniform float uIntensity;

half4 main(float2 fragCoord) {
    float row = floor(fragCoord.y / 6.0);
    float band = sin(row * 0.85 + uTime * 11.0);
    float burst = step(0.84, fract(sin(row * 19.3 + floor(uTime * 8.0)) * 43758.5453));
    float shift = (band * 1.4 + burst * 4.5) * uIntensity;

    float2 uvR = float2(clamp(fragCoord.x + shift, 0.0, uResolution.x - 1.0), fragCoord.y);
    float2 uvG = fragCoord;
    float2 uvB = float2(clamp(fragCoord.x - shift, 0.0, uResolution.x - 1.0), fragCoord.y);

    half4 cR = contents.eval(uvR);
    half4 cG = contents.eval(uvG);
    half4 cB = contents.eval(uvB);

    float scan = 1.0 - 0.08 * uIntensity * step(0.5, fract(fragCoord.y * 0.25));
    return half4(cR.r * scan, cG.g * scan, cB.b * scan, cG.a);
}
"""

@Language("AGSL")
private const val COMBAT_IMPACT_AGSL = """
uniform shader contents;
uniform float2 uResolution;
uniform float uImpact;
uniform float uIsDamage;

half4 main(float2 fragCoord) {
    float2 center = uResolution * 0.5;
    float2 fromCenter = fragCoord - center;
    float distNorm = length(fromCenter) / max(1.0, length(center));

    float shift = uImpact * (3.0 + distNorm * 4.0);
    float2 pR = float2(clamp(fragCoord.x + shift, 0.0, uResolution.x - 1.0), fragCoord.y);
    float2 pB = float2(clamp(fragCoord.x - shift, 0.0, uResolution.x - 1.0), fragCoord.y);

    half4 base = contents.eval(fragCoord);
    half r = contents.eval(pR).r;
    half b = contents.eval(pB).b;

    half3 rgb = half3(r, base.g, b);
    if (uIsDamage > 0.5) {
        float vignette = smoothstep(0.35, 1.05, distNorm) * uImpact * 0.55;
        rgb = mix(rgb, half3(1.0, 0.14, 0.18), half(vignette));
    } else {
        float flash = uImpact * 0.22;
        rgb = mix(rgb, half3(1.0, 0.96, 0.72), half(flash));
    }
    return half4(rgb, base.a);
}
"""

/**
 * Android 13+ (API 33+) AGSL pixel-art glitch chromatic scanline post-process shader.
 * Safely degrades to no-op on API < 33 or whenever `reduceFlash` is enabled.
 */
fun Modifier.retroGlitchShaderEffect(
    timeSec: Float,
    intensity: Float,
    reduceFlash: Boolean
): Modifier = composed {
    if (reduceFlash || intensity <= 0.01f || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return@composed this
    }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val shader = remember {
        runCatching { RuntimeShader(GLITCH_SCAN_AGSL) }.getOrNull()
    } ?: return@composed this

    this
        .onSizeChanged { boxSize = it }
        .graphicsLayer {
            if (boxSize.width > 1 && boxSize.height > 1) {
                runCatching {
                    shader.setFloatUniform("uResolution", boxSize.width.toFloat(), boxSize.height.toFloat())
                    shader.setFloatUniform("uTime", timeSec)
                    shader.setFloatUniform("uIntensity", intensity.coerceIn(0f, 1.5f))
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "contents")
                        .asComposeRenderEffect()
                }
            }
        }
}

/**
 * Android 13+ (API 33+) AGSL combat impact chromatic aberration & edge vignette pulse shader.
 * Safely degrades to no-op on API < 33 or whenever `reduceFlash` is enabled.
 */
fun Modifier.combatImpactShaderEffect(
    impactStrength: Float,
    isDamage: Boolean,
    reduceFlash: Boolean
): Modifier = composed {
    if (reduceFlash || impactStrength <= 0.01f || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return@composed this
    }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val shader = remember {
        runCatching { RuntimeShader(COMBAT_IMPACT_AGSL) }.getOrNull()
    } ?: return@composed this

    this
        .onSizeChanged { boxSize = it }
        .graphicsLayer {
            if (boxSize.width > 1 && boxSize.height > 1) {
                runCatching {
                    shader.setFloatUniform("uResolution", boxSize.width.toFloat(), boxSize.height.toFloat())
                    shader.setFloatUniform("uImpact", impactStrength.coerceIn(0f, 1.5f))
                    shader.setFloatUniform("uIsDamage", if (isDamage) 1f else 0f)
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "contents")
                        .asComposeRenderEffect()
                }
            }
        }
}
