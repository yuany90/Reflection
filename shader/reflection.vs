varying vec3 worldVertexPosition;
varying vec3 worldNormalDirection;
mat3 GetLinearPart( mat4 m )
{
   mat3 result;
	
   result[0][0] = m[0][0]; 
   result[0][1] = m[0][1]; 
   result[0][2] = m[0][2]; 

   result[1][0] = m[1][0]; 
   result[1][1] = m[1][1]; 
   result[1][2] = m[1][2]; 
	
   result[2][0] = m[2][0]; 
   result[2][1] = m[2][1]; 
   result[2][2] = m[2][2]; 
	
   return result;
}

void main()
{
	gl_Position=gl_ModelViewProjectionMatrix*gl_Vertex;
	worldVertexPosition=vec3(gl_ModelViewMatrix*gl_Vertex);
	worldNormalDirection=GetLinearPart(gl_ModelViewMatrix)*gl_Normal.xyz;
}
