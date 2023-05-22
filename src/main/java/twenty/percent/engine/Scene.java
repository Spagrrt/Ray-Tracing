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

    private String fragmentShaderSrc = "#version 330 core\n" +
            "\n" +
            "#define PI 3.1415926535\n" +
            "#define FOV 75.0\n" +
            "#define RATIO 16.0/9.0\n" +
            "\n" +
            "struct sphere{\n" +
            "    vec3 position;\n" +
            "    float radius;\n" +
            "    vec3 color;\n" +
            "};\n" +
            "\n" +
            "sphere[] sphereList = { {vec3(0.0, 0.0, 3.0), 1.0, vec3(0.0, 1.0, 0.0)},\n" +
            "                        {vec3(-2.5, 0.0, 3.0), 1.0, vec3(1.0, 0.0, 0.0)},\n" +
            "                        {vec3(2.5, 0.0, 3.0), 1.0, vec3(0.0, 0.0, 1.0)}};\n" +
            "\n" +
            "\n" +
            "// credit to wwwtyro on github\n" +
            "float raySphereIntersect(vec3 r0, vec3 rd, vec3 s0, float sr) {\n" +
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
            "vec3 normalize(vec3 terminal){\n" +
            "    float magnitude = sqrt(terminal.x * terminal.x + terminal.y * terminal.y + terminal.z + terminal.z);\n" +
            "    return vec3(terminal.x / magnitude, terminal.y / magnitude, terminal.z / magnitude);\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    vec3 color = vec3(0.0, 0.0, 0.0);\n" +
            "\n" +
            "    vec2 fragCoord = vec2(gl_FragCoord.x / (1920.0f / 2.0f) - 1.0f, gl_FragCoord.y / (1080.0f / 2.0f) - 1.0f);\n" +
            "\n" +
            "    float planeWidth = tan(FOV * 0.5 * (PI/180.0)) * 2.0;\n" +
            "    float planeHeight = planeWidth * RATIO;\n" +
            "    vec3 raydir = vec3(planeHeight * fragCoord.x, planeWidth * fragCoord.y, 1);\n" +
            "    vec3 nraydir = normalize(raydir);\n" +
            "\n" +
            "    for(int i = 0; i < sphereList.length(); i++) {\n" +
            "        if(raySphereIntersect(vec3(0.0, 0.0, 0.0), nraydir, sphereList[i].position, sphereList[i].radius) >= 0){\n" +
            "            color = vec3(sphereList[i].color);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
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