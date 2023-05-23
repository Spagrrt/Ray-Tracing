#type fragment
#version 330 core

#define PI 3.1415926535
#define FOV 75.0
#define RATIO 16.0/9.0

struct Ray{
    vec3 origin;
    vec3 direction;
};

struct Material{
    vec3 color;
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
    Ray normal;
    Material material;
};

Sphere[] sphereList = { { vec3(0.0, 0.25, 3.0), 1.0, { vec3(0.0, 1.0, 0.0) }},
                        { vec3(-0.75, 0.0, 3.0), 1.0, { vec3(1.0, 0.0, 0.0) }},
                        { vec3(0.75, 0.0, 3.0), 1.0, { vec3(0.0, 0.0, 1.0) }}
};




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
    vec3 dif = ray.direction - ray.origin;
    float magnitude = sqrt(dif.x * dif.x + dif.y * dif.y + dif.z * dif.z);

    Ray newRay;
    newRay.origin = ray.origin;
    newRay.direction = vec3((dif.x / magnitude) * len, (dif.y / magnitude) * len, (dif.z / magnitude) * len) + ray.origin;
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
        hitInfo.hitPoint = SetRayMagnitude(ray, dist).direction;

        Ray normalRay;
        normalRay.origin = sphere.position;
        normalRay.direction = hitInfo.hitPoint;

        hitInfo.normal = SetRayMagnitude(normalRay, 1.0);
    }
    return hitInfo;
}

void main() {

    vec3 color = vec3(0.0, 0.0, 0.0);

    vec2 fragCoord = vec2(gl_FragCoord.x / (1920.0f / 2.0f) - 1.0f, gl_FragCoord.y / (1080.0f / 2.0f) - 1.0f);

    float planeWidth = tan(FOV * 0.5 * (PI/180.0)) * 2.0;
    float planeHeight = planeWidth * RATIO;

    Ray ray;
    ray.origin = vec3(0.0, 0.0, 0.0);
    ray.direction = vec3(planeHeight * fragCoord.x, planeWidth * fragCoord.y, 1);

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

    if(closestHit.didHit){
        color = closestHit.material.color;
    }

    gl_FragColor = vec4(color, 1.0);
}