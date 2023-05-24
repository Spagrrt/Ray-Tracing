#type fragment
#version 330

#define PI 3.1415926535
#define FOV 75.0
#define WIDTH 1920
#define HEIGHT 1080
#define RATIO WIDTH/HEIGHT

uint PixelIndex(){
    return uint((gl_FragCoord.y * WIDTH) + gl_FragCoord.x);
}

float RandomValue(inout uint state){
    state = state * uint(747796405) + uint(2891336453);
    uint result = ((state >> ((state >> 28) + uint(4))) ^ state) * uint(277803737);
    result = (result >> 22) ^ result;
    return result / 4294967295.0;
}

void main() {

    uint rngState = PixelIndex();
    float r = RandomValue(rngState);
    float g = RandomValue(rngState);
    float b = RandomValue(rngState);

    gl_FragColor = vec4(r, g, b, 1.0);
}
