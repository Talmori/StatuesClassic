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

void main() {
    float grayScale = dot(vertexColor.rgb, vec3(0.299, 0.587, 0.114));
    fragColor = vec4(vec3(grayScale * overlayColor), 1.0);
}
