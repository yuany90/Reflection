varying vec3 worldVertexPosition;
varying vec3 worldNormalDirection;

uniform samplerCube cubeMap;
uniform vec3 cameraPosition;
uniform float reflectFactor;
void main()
{
    float refractiveIndex = 10;
    vec3 viewDirection= worldVertexPosition-cameraPosition;
    vec3 incident=normalize(viewDirection);
    vec3 normal=normalize(worldNormalDirection);
    vec3 refracted=refract(incident,normal, 1.0 / refractiveIndex);
    vec3 reflected=reflect(viewDirection,normal);
    vec3 refractedColor = textureCube(cubeMap, refracted).rgb;
    vec3 reflectedColor = textureCube(cubeMap, reflected).rgb;
    //gl_FragColor= vec4(refractedColor, 1.0);
    //gl_FragColor= vec4(reflectedColor, 1.0);
    gl_FragColor= vec4(mix(refractedColor, reflectedColor, reflectFactor), 0.0);
    //gl_FragColor=vec4(0.0, 1.0, 1.0 ,1.0);
}
