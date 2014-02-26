void main()
{
   //skyTexCoords = normalize(gl_Vertex);

   gl_TexCoord[0].xyz = gl_Vertex.xyz * vec3(1.0, 1.0, 1.0);

   //gl_TexCoord[0] = gl_MultiTexCoord0;
   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}