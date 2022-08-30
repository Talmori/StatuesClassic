#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

vec3 toHSV(vec3 c)
{
    vec4 k = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, k.wz), vec4(c.gb, k.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 toRGB(vec3 c)
{
    vec4 k = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + k.xyz) * 6.0 - k.www);
    return c.z * mix(k.xxx, clamp(p - k.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    float alpha = color.a;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);


    vec3 hsv = toHSV(color.rgb);
    float sat = hsv.y;
    float value = hsv.z;

    float cutoff = 0.75;
    float satBuffCutoff = 0.4;

    if (sat < satBuffCutoff) {
        value += satBuffCutoff-sat;
    }

    if (value < cutoff) {
        value += (cutoff-value) / 1.5;
    }

    hsv.y = 0.0;
    hsv.z = value;

    fragColor = linear_fog(vec4(toRGB(hsv), alpha) * vertexColor * ColorModulator * lightMapColor, vertexDistance, FogStart, FogEnd, FogColor);
}
