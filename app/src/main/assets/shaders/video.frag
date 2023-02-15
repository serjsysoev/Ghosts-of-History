#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 v_TexCoord;
uniform samplerExternalOES sTexture;

void main() {
  vec4 color = texture2D(sTexture, v_TexCoord);
  float red = 1.0;
  float green = 1.0;
  float blue = 1.0;
  float accuracy = 0.3;
  if (abs(color.r - red) <= accuracy && abs(color.g - green) <= accuracy && abs(color.b - blue) <= accuracy) {
      gl_FragColor = vec4(color.r, color.g, color.b, 0.0);
  } else {
      gl_FragColor = vec4(color.r, color.g, color.b, 1.0);
  }
}
