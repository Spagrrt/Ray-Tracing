#type fragment
#version 330

#define PI 3.1415926535
#define FOV 75.0
#define WIDTH 1920
#define HEIGHT 1080
#define RATIO WIDTH/HEIGHT
#define MAX_BOUNCE 10
#define RAYS_PER_PIXEL 250

struct Ray{
    vec3 origin;
    vec3 direction;
};

struct Material{
    vec3 color;
    vec3 emissionColor;
    float emissionStrength;
};

struct Sphere{
    vec3 position;
    float radius;
    Material material;
};

struct HitInfo{
    bool didHit;
    float dist;
    vec3 hitPoint;
    vec3 normal;
    Material material;
};

Sphere[] sphereList = { { vec3(0.0, 3.0, 10.0), 3.0, { vec3(0.0, 0.0, 0.0), vec3(1.0, 1.0, 1.0), 3.0 }},
{ vec3(-2.75, 0.0, 3.0), 1.0, { vec3(1.0, 0.0, 0.0), vec3(0.0, 0.0, 0.0), 0.0 }},
{ vec3(2.75, 0.0, 3.0), 1.0, { vec3(0.0, 0.0, 1.0), vec3(0.0, 0.0, 0.0), 0.0 }},
{ vec3(0.0, -10.0, 5.0), 9.0, { vec3(1.0, 0.0, 1.0), vec3(0.0, 0.0, 0.0), 0.0 }}
};



uint PixelIndex(){
    return uint((gl_FragCoord.y * WIDTH) + gl_FragCoord.x);
}

float RandomValue(inout uint state){
    state = state * uint(747796405) + uint(2891336453);
    uint result = ((state >> ((state >> 28) + uint(4))) ^ state) * uint(277803737);
    result = (result >> 22) ^ result;
    return result / 4294967295.0;
}

// credit to wwwtyro on github
float RaySphereIntersect(vec3 r0, vec3 rd, vec3 s0, float sr) {
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

Ray SetRayMagnitude(Ray ray, float len){
    vec3 dir = ray.direction;
    float magnitude = sqrt(dir.x * dir.x + dir.y * dir.y + dir.z * dir.z);

    Ray newRay;
    newRay.origin = ray.origin;
    newRay.direction = vec3((dir.x / magnitude) * len, (dir.y / magnitude) * len, (dir.z / magnitude) * len);
    return newRay;
}

HitInfo RaySphere(Ray ray, Sphere sphere){
    HitInfo hitInfo;
    ray = SetRayMagnitude(ray, 1.0);
    float dist = RaySphereIntersect(ray.origin, ray.direction, sphere.position, sphere.radius);

    hitInfo.didHit = false;

    if(dist >= 0){
        hitInfo.didHit = true;
        hitInfo.dist = dist;
        hitInfo.hitPoint = SetRayMagnitude(ray, dist).direction + ray.origin;
        hitInfo.normal = hitInfo.hitPoint - sphere.position;
    }
    return hitInfo;
}

vec3 RandomDirection(inout uint state){
    while(true){
        float x = RandomValue(state) * 2 - 1;
        float y = RandomValue(state) * 2 - 1;
        float z = RandomValue(state) * 2 - 1;
        vec3 pointInCube = vec3(x, y, z);
        float sqrDstFromCenter = dot(pointInCube, pointInCube);
        if (sqrDstFromCenter <= 1){
            return pointInCube / sqrt(sqrDstFromCenter);
        }
    }
}

vec3 RandomHemisphereDirection(vec3 normal, inout uint state){
    vec3 dir = RandomDirection(state);
    return dir * sign(dot(normal, dir));
}

HitInfo CalculateRayCollision(Ray ray){
    HitInfo closestHit;
    closestHit.didHit = false;

    for(int i = 0; i < sphereList.length(); i++) {
        HitInfo thisHit = RaySphere(ray, sphereList[i]);
        if(thisHit.didHit){
            if(thisHit.dist < closestHit.dist || !closestHit.didHit){
                closestHit = thisHit;
                closestHit.material = sphereList[i].material;
            }
        }
    }
    return closestHit;
}

vec3 Trace(Ray ray, inout uint state){

    vec3 incomingLight = vec3(0.0, 0.0, 0.0);
    vec3 rayColor = vec3(1.0, 1.0, 1.0);

    for(int i = 0; i <= MAX_BOUNCE; i++){
        HitInfo hitinfo = CalculateRayCollision(ray);
        if(hitinfo.didHit){
            ray.origin = hitinfo.hitPoint;
            ray.direction = RandomHemisphereDirection(hitinfo.normal, state);

            Material material = hitinfo.material;
            vec3 emittedLight = material.emissionColor * material.emissionStrength;
            incomingLight += emittedLight * rayColor;
            rayColor *= material.color;
        }
        else{
            break;
        }
    }

    return incomingLight;
}

void main() {

    uint RNGState = PixelIndex();

    vec2 fragCoord = vec2(gl_FragCoord.x / WIDTH - 0.5f, gl_FragCoord.y / HEIGHT - 0.5f);

    float planeWidth = tan(FOV * 0.5 * (PI/180.0)) * 2.0;
    float planeHeight = planeWidth * RATIO;

    Ray ray;
    ray.origin = vec3(0.0, 0.0, 0.0);
    ray.direction = vec3(planeHeight * fragCoord.x, planeWidth * fragCoord.y, 1);

    vec3 totalIncomingLight = vec3(0.0, 0.0, 0.0);

    for(int i = 0; i < RAYS_PER_PIXEL; i++){
        totalIncomingLight += Trace(ray, RNGState);
    }
    vec3 color = totalIncomingLight / RAYS_PER_PIXEL;
    gl_FragColor = vec4(color, 1.0);
}