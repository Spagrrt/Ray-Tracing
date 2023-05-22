#type fragment
#version 330 core

#define PI 3.1415926535
#define FOV 75.0
#define RATIO 16.0/9.0

struct sphere{
    vec3 position;
    float radius;
    vec3 color;
};

sphere[] sphereList = { {vec3(0.0, 0.0, 3.0), 1.0, vec3(0.0, 1.0, 0.0)},
                        {vec3(-2.5, 0.0, 3.0), 1.0, vec3(1.0, 0.0, 0.0)},
                        {vec3(2.5, 0.0, 3.0), 1.0, vec3(0.0, 0.0, 1.0)}};


// credit to wwwtyro on github
float raySphereIntersect(vec3 r0, vec3 rd, vec3 s0, float sr) {
    // - r0: ray origin
    // - rd: normalized ray direction
    // - s0: sphere center
    // - sr: sphere radius
    // - Returns distance from r0 to first intersecion with sphere,
    //   or -1.0 if no intersection.
    float a = dot(rd, rd);
    vec3 s0_r0 = r0 - s0;
    float b = 2.0 * dot(rd, s0_r0);
    float c = dot(s0_r0, s0_r0) - (sr * sr);
    if (b*b - 4.0*a*c < 0.0) {
        return -1.0;
    }
    return (-b - sqrt((b*b) - 4.0*a*c))/(2.0*a);
}

vec3 normalize(vec3 terminal){
    float magnitude = sqrt(terminal.x * terminal.x + terminal.y * terminal.y + terminal.z + terminal.z);
    return vec3(terminal.x / magnitude, terminal.y / magnitude, terminal.z / magnitude);
}

void main() {

    vec3 color = vec3(0.0, 0.0, 0.0);

    vec2 fragCoord = vec2(gl_FragCoord.x / (1920.0f / 2.0f) - 1.0f, gl_FragCoord.y / (1080.0f / 2.0f) - 1.0f);

    float planeWidth = tan(FOV * 0.5 * (PI/180.0)) * 2.0;
    float planeHeight = planeWidth * RATIO;
    vec3 raydir = vec3(planeHeight * fragCoord.x, planeWidth * fragCoord.y, 1);
    vec3 nraydir = normalize(raydir);

    for(int i = 0; i < sphereList.length(); i++) {
        if(raySphereIntersect(vec3(0.0, 0.0, 0.0), nraydir, sphereList[i].position, sphereList[i].radius) >= 0){
            color = vec3(sphereList[i].color);
        }
    }

    gl_FragColor = vec4(color, 1.0);
}