uniform samplerCube cubeMap;

void main()
{
   //vec4 env = texture2D(cubeMap, gl_TexCoord[0].st);
   //gl_FragColor = env;


   //gl_FragColor = textureCube(cubeMap, gl_TexCoord[0]);

   vec3 cube = vec3(textureCube(cubeMap, gl_TexCoord[0].xyz));
   gl_FragColor = vec4(cube, 0.5);
   
   //gl_FragColor = textureCube(cubeMap, skyTexCoords) * vec4(0.5,0,0,0);
   //gl_FragColor = vec4(0.5,0,0,0);
}