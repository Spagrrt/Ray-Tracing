#type fragment
#version 330 core

void main(){
    gl_FragColor = vec4(gl_FragCoord.x / 1920.0, 0.0, gl_FragCoord.y / 1080.0, 1.0);
}