package twenty.percent.engine;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Scene{

    private String vertexShaderSrc = "#version 330 core\n" +
            "layout (location=0) in vec3 aPos;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}";

    private String fragmentShaderSrc = "#version 330\n" +
            "\n" +
            "#define PI 3.1415926535\n" +
            "#define FOV 75.0\n" +
            "#define WIDTH 1920\n" +
            "#define HEIGHT 1080\n" +
            "#define RATIO WIDTH/HEIGHT\n" +
            "#define MAX_BOUNCE 10\n" +
            "#define RAYS_PER_PIXEL 250\n" +
            "\n" +
            "struct Ray{\n" +
            "    vec3 origin;\n" +
            "    vec3 direction;\n" +
            "};\n" +
            "\n" +
            "struct Material{\n" +
            "    vec3 color;\n" +
            "    vec3 emissionColor;\n" +
            "    float emissionStrength;\n" +
            "};\n" +
            "\n" +
            "struct Sphere{\n" +
            "    vec3 position;\n" +
            "    float radius;\n" +
            "    Material material;\n" +
            "};\n" +
            "\n" +
            "struct HitInfo{\n" +
            "    bool didHit;\n" +
            "    float dist;\n" +
            "    vec3 hitPoint;\n" +
            "    vec3 normal;\n" +
            "    Material material;\n" +
            "};\n" +
            "\n" +
            "Sphere[] sphereList = { { vec3(0.0, 3.0, 10.0), 3.0, { vec3(0.0, 0.0, 0.0), vec3(1.0, 1.0, 1.0), 3.0 }},\n" +
            "{ vec3(-2.75, 0.0, 3.0), 1.0, { vec3(1.0, 0.0, 0.0), vec3(0.0, 0.0, 0.0), 0.0 }},\n" +
            "{ vec3(2.75, 0.0, 3.0), 1.0, { vec3(0.0, 0.0, 1.0), vec3(0.0, 0.0, 0.0), 0.0 }},\n" +
            "{ vec3(0.0, -10.0, 5.0), 9.0, { vec3(1.0, 0.0, 1.0), vec3(0.0, 0.0, 0.0), 0.0 }}\n" +
            "};\n" +
            "\n" +
            "\n" +
            "\n" +
            "uint PixelIndex(){\n" +
            "    return uint((gl_FragCoord.y * WIDTH) + gl_FragCoord.x);\n" +
            "}\n" +
            "\n" +
            "float RandomValue(inout uint state){\n" +
            "    state = state * uint(747796405) + uint(2891336453);\n" +
            "    uint result = ((state >> ((state >> 28) + uint(4))) ^ state) * uint(277803737);\n" +
            "    result = (result >> 22) ^ result;\n" +
            "    return result / 4294967295.0;\n" +
            "}\n" +
            "\n" +
            "// credit to wwwtyro on github\n" +
            "float RaySphereIntersect(vec3 r0, vec3 rd, vec3 s0, float sr) {\n" +
            "    // - r0: ray origin\n" +
            "    // - rd: normalized ray direction\n" +
            "    // - s0: sphere center\n" +
            "    // - sr: sphere radius\n" +
            "    // - Returns distance from r0 to first intersecion with sphere,\n" +
            "    //   or -1.0 if no intersection.\n" +
            "    float a = dot(rd, rd);\n" +
            "    vec3 s0_r0 = r0 - s0;\n" +
            "    float b = 2.0 * dot(rd, s0_r0);\n" +
            "    float c = dot(s0_r0, s0_r0) - (sr * sr);\n" +
            "    if (b*b - 4.0*a*c < 0.0) {\n" +
            "        return -1.0;\n" +
            "    }\n" +
            "    return (-b - sqrt((b*b) - 4.0*a*c))/(2.0*a);\n" +
            "}\n" +
            "\n" +
            "Ray SetRayMagnitude(Ray ray, float len){\n" +
            "    vec3 dir = ray.direction;\n" +
            "    float magnitude = sqrt(dir.x * dir.x + dir.y * dir.y + dir.z * dir.z);\n" +
            "\n" +
            "    Ray newRay;\n" +
            "    newRay.origin = ray.origin;\n" +
            "    newRay.direction = vec3((dir.x / magnitude) * len, (dir.y / magnitude) * len, (dir.z / magnitude) * len);\n" +
            "    return newRay;\n" +
            "}\n" +
            "\n" +
            "HitInfo RaySphere(Ray ray, Sphere sphere){\n" +
            "    HitInfo hitInfo;\n" +
            "    ray = SetRayMagnitude(ray, 1.0);\n" +
            "    float dist = RaySphereIntersect(ray.origin, ray.direction, sphere.position, sphere.radius);\n" +
            "\n" +
            "    hitInfo.didHit = false;\n" +
            "\n" +
            "    if(dist >= 0){\n" +
            "        hitInfo.didHit = true;\n" +
            "        hitInfo.dist = dist;\n" +
            "        hitInfo.hitPoint = SetRayMagnitude(ray, dist).direction + ray.origin;\n" +
            "        hitInfo.normal = hitInfo.hitPoint - sphere.position;\n" +
            "    }\n" +
            "    return hitInfo;\n" +
            "}\n" +
            "\n" +
            "vec3 RandomDirection(inout uint state){\n" +
            "    while(true){\n" +
            "        float x = RandomValue(state) * 2 - 1;\n" +
            "        float y = RandomValue(state) * 2 - 1;\n" +
            "        float z = RandomValue(state) * 2 - 1;\n" +
            "        vec3 pointInCube = vec3(x, y, z);\n" +
            "        float sqrDstFromCenter = dot(pointInCube, pointInCube);\n" +
            "        if (sqrDstFromCenter <= 1){\n" +
            "            return pointInCube / sqrt(sqrDstFromCenter);\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "vec3 RandomHemisphereDirection(vec3 normal, inout uint state){\n" +
            "    vec3 dir = RandomDirection(state);\n" +
            "    return dir * sign(dot(normal, dir));\n" +
            "}\n" +
            "\n" +
            "HitInfo CalculateRayCollision(Ray ray){\n" +
            "    HitInfo closestHit;\n" +
            "    closestHit.didHit = false;\n" +
            "\n" +
            "    for(int i = 0; i < sphereList.length(); i++) {\n" +
            "        HitInfo thisHit = RaySphere(ray, sphereList[i]);\n" +
            "        if(thisHit.didHit){\n" +
            "            if(thisHit.dist < closestHit.dist || !closestHit.didHit){\n" +
            "                closestHit = thisHit;\n" +
            "                closestHit.material = sphereList[i].material;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    return closestHit;\n" +
            "}\n" +
            "\n" +
            "vec3 Trace(Ray ray, inout uint state){\n" +
            "\n" +
            "    vec3 incomingLight = vec3(0.0, 0.0, 0.0);\n" +
            "    vec3 rayColor = vec3(1.0, 1.0, 1.0);\n" +
            "\n" +
            "    for(int i = 0; i <= MAX_BOUNCE; i++){\n" +
            "        HitInfo hitinfo = CalculateRayCollision(ray);\n" +
            "        if(hitinfo.didHit){\n" +
            "            ray.origin = hitinfo.hitPoint;\n" +
            "            ray.direction = RandomHemisphereDirection(hitinfo.normal, state);\n" +
            "\n" +
            "            Material material = hitinfo.material;\n" +
            "            vec3 emittedLight = material.emissionColor * material.emissionStrength;\n" +
            "            incomingLight += emittedLight * rayColor;\n" +
            "            rayColor *= material.color;\n" +
            "        }\n" +
            "        else{\n" +
            "            break;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    return incomingLight;\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    uint RNGState = PixelIndex();\n" +
            "\n" +
            "    vec2 fragCoord = vec2(gl_FragCoord.x / WIDTH - 0.5f, gl_FragCoord.y / HEIGHT - 0.5f);\n" +
            "\n" +
            "    float planeWidth = tan(FOV * 0.5 * (PI/180.0)) * 2.0;\n" +
            "    float planeHeight = planeWidth * RATIO;\n" +
            "\n" +
            "    Ray ray;\n" +
            "    ray.origin = vec3(0.0, 0.0, 0.0);\n" +
            "    ray.direction = vec3(planeHeight * fragCoord.x, planeWidth * fragCoord.y, 1);\n" +
            "\n" +
            "    vec3 totalIncomingLight = vec3(0.0, 0.0, 0.0);\n" +
            "\n" +
            "    for(int i = 0; i < RAYS_PER_PIXEL; i++){\n" +
            "        totalIncomingLight += Trace(ray, RNGState);\n" +
            "    }\n" +
            "    vec3 color = totalIncomingLight / RAYS_PER_PIXEL;\n" +
            "    gl_FragColor = vec4(color, 1.0);\n" +
            "}";
    private int vertexID, fragmentID, shaderProgram;

    private float[] vertexArray = {
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
    };

    //IMPORTANT: Must be in counter-clockwise order
    private int[] elementArray = {
            0, 1, 2,
            2, 3, 0
    };

    private int vaoID, vboID, eboID;

    public Scene(){

    }

    public void init() {
        //Compile and link shaders

        //Load and compile the vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        //pass shader source code to GPU
        glShaderSource(vertexID, vertexShaderSrc);
        glCompileShader(vertexID);

        //Check for errors in compilation
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE){
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: 'test.glsl'\n\tVertex shader compilation failed");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        //Load and compile the fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        //pass shader source code to GPU
        glShaderSource(fragmentID, fragmentShaderSrc);
        glCompileShader(fragmentID);

        //Check for errors in compilation
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE){
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: 'test.glsl'\n\tFragment shader compilation failed");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        //Link shaders and check for errors
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexID);
        glAttachShader(shaderProgram, fragmentID);
        glLinkProgram(shaderProgram);

        //Check for linking errors
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE){
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("Error: 'test.glsl'\n\tShader program linking failed");
            System.out.println(glGetProgramInfoLog(shaderProgram, len));
            assert false : "";
        }


        //Generate VAO, VBO, and EBO buffer objects and send to GPU
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        //Create float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        //Create VBO and upload the vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        //Create indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        //Add the vertex attribute pointers
        int positionSize = 3;
        int floatSizeBytes = 4;
        int vertexSizeBytes = positionSize * floatSizeBytes;
        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);
    }

    public void update() {
        //Bind shader program
        glUseProgram(shaderProgram);
        //Bind VAO
        glBindVertexArray(vaoID);

        //Enable vertex attribute pointers
        glEnableVertexAttribArray(0);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        //Unbind everything
        glDisableVertexAttribArray(0);

        glBindVertexArray(0);

        glUseProgram(0);
    }
}