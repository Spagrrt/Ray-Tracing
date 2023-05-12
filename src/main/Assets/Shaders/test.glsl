#type vertex
#version 330 core
layout (location=0) in vec3 aPos;

void main(){
    gl_Position = vec4(aPos, 1.0);
}

#type fragment
#version 330 core

void main(){
    gl_FragColor = vec4(gl_FragCoord.x / 1920.0, 0.0, gl_FragCoord.y / 1080.0, 1.0);
}