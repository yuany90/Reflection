/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yourorghere;

import com.sun.opengl.util.texture.Texture;

import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.TextureData;
import javax.media.opengl.GL;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import java.io.*;
import javax.media.opengl.glu.GLUquadric;
import java.nio.IntBuffer;
import java.util.ArrayList;
import com.sun.opengl.util.texture.*;
/**
 *
 * @author yuanlu
 */
public class GLRender extends MouseAdapter implements KeyListener, GLEventListener{
    private String cubeMapFileName[] =
    {  "skybox/posx.jpg", 
       "skybox/negx.jpg",
       "skybox/posy.jpg",
       "skybox/negy.jpg",
       "skybox/posz.jpg",
       "skybox/negz.jpg",
     };
    private Texture texture[];
    private Texture cube;
    private GL gl;
    private GLU glu = new GLU();
    private GLUT glut = new GLUT();
    private float cameraPos[] = {0.0f, 1.0f, -0.06f};
    private float targetPos[] = {0.0f, 1.0f, 20.0f};
    private float alpha = 0.0f;
    private float belta = 1.57f;
    private float woverh;

    public void rotation(){
        targetPos[0] = cameraPos[0] + 20.0f * (float)Math.cos(alpha) * (float)Math.cos(belta);
        targetPos[1] = cameraPos[1] + 20.0f * (float)Math.sin(alpha);
        targetPos[2] = cameraPos[2] + 20.0f * (float)Math.cos(alpha) * (float)Math.sin(belta);
    };

    int light1[] = new int[8];
    int reflectionObject, skyProgramObject;
    int samplerDiffuse, samplerSpecular, samplerNormal, samplerCubeMap;
    int eyePosW, samplerSky,skyShaderV, skyShaderF, litColor;
    int samplerPostColor, samplerPostDepth, uniformWidth, uniformHeight, uniformFocalLength;
    int cameraPosition, reflectCubeMap, reflectFactor;
    float reflectPrecent = 0.0f;
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL();
        try{
            loadAllTexture();
            loadAllShader();
        } catch(IOException e){
            System.err.println("Texture load failed.");
            System.exit(0);
        }

        
        gl.setSwapInterval(1);
        // Setup the drawing area and shading mode
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glShadeModel(GL.GL_SMOOTH); // try setting this to GL_FLAT and see what happens.
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        skyProgramObject = gl.glCreateProgram();
        gl.glAttachShader(skyProgramObject, skyShaderV);
        gl.glAttachShader(skyProgramObject, skyShaderF);
        gl.glLinkProgram(skyProgramObject);
        gl.glValidateProgram(skyProgramObject);
        
        samplerSky = gl.glGetUniformLocationARB(skyProgramObject, "cubeMap");

        reflectionObject = gl.glCreateProgram();
        gl.glAttachShader(reflectionObject, reflectV);
        gl.glAttachShader(reflectionObject, reflectF);
        gl.glLinkProgram(reflectionObject);
        gl.glValidateProgram(reflectionObject);

        cameraPosition = gl.glGetUniformLocationARB(reflectionObject, "cameraPosition");
	reflectCubeMap = gl.glGetUniformLocationARB(reflectionObject,"cubeMap");
        reflectFactor = gl.glGetUniformLocationARB(reflectionObject,"reflectFactor");
    }
    int light;
    private void drawSphere(){
        gl.glUseProgram(reflectionObject);
        gl.glUniform1i(reflectCubeMap, 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glUniform3fARB(cameraPosition, cameraPos[0], cameraPos[1], cameraPos[2]);
        gl.glUniform1fARB(reflectFactor, reflectPrecent);
        cube.bind();
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, 1.0f ,0.0f);;
        glut.glutSolidSphere(0.01,32,32); 
    //    glu.gluSphere(null, 0.01,64,64);
        gl.glPopMatrix();
        gl.glUseProgram(0);
    }

    public void drawSkyBox(){
         //Forward
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glUseProgram(skyProgramObject);
        gl.glUniform1i(samplerSky, 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
//        gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, cubeMap);
        cube.bind();
        gl.glPushMatrix();
        gl.glTranslated(cameraPos[0], cameraPos[1], cameraPos[2]);
    //    gl.glTranslated(0.0f, 1.0f, 0.0f);
        gl.glBegin(GL.GL_QUADS);
        // Front Face
        gl.glNormal3d(0, 0, 1);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Back Face
        gl.glNormal3d(0, 0, -1);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Top Face
        gl.glNormal3d(0, 1, 0);
        gl.glTexCoord2f(0.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(1.0f, -1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Bottom Face
        gl.glNormal3d(0, -1, 0);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Right face
        gl.glNormal3d(1, 0, 0);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glEnd();

        gl.glBegin(GL.GL_QUADS);
        // Left Face
        gl.glNormal3d(-1, 0, 0);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(-1.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(-1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glUseProgram(0);
        gl.glEnable(GL.GL_DEPTH_TEST);
    }
    
    public void display(GLAutoDrawable drawable) {
        // Clear the drawing area
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60.0f, woverh, 0.01, 100.0);
        rotation();
        glu.gluLookAt(cameraPos[0], cameraPos[1], cameraPos[2],
            targetPos[0], targetPos[1], targetPos[2], 0, 1, 0);
        // Reset the current matrix to the "identity"
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        float LightAmbient[]= { 0.5f, 0.5f, 1.0f, 1.0f }; 
        float LightDiffuse[]= { 1.0f, 1.0f, 1.0f, 1.0f };
        float LightPosition[]= { cameraPos[0], cameraPos[1], cameraPos[2], 1.0f }; 
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, LightAmbient,0); 
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, LightDiffuse,0); 
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION,LightPosition,0); 
        drawSkyBox();
        drawSphere();
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();
        if (height <= 0) { // avoid a divide by zero error!
            height = 1;
        }
        woverh = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60.0f, woverh, 0.01, 100.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public Texture loadCubeTextFromFile(String[] fileName) throws IOException{
        Texture cube = TextureIO.newTexture(GL.GL_TEXTURE_CUBE_MAP);
        TextureData posx = TextureIO.newTextureData(new File(fileName[0]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, TextureIO.JPG);
        TextureData negx = TextureIO.newTextureData(new File(fileName[1]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, TextureIO.JPG);
        TextureData posy = TextureIO.newTextureData(new File(fileName[2]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false,  TextureIO.JPG);
        TextureData negy = TextureIO.newTextureData(new File(fileName[3]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false,  TextureIO.JPG);
        TextureData posz = TextureIO.newTextureData(new File(fileName[4]), GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,false,  TextureIO.JPG);
        TextureData negz = TextureIO.newTextureData(new File(fileName[5]),GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, TextureIO.JPG);
        
        cube.updateImage(posx, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
        cube.updateImage(negx, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
        cube.updateImage(posy, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
        cube.updateImage(negy, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
        cube.updateImage(posz, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
        cube.updateImage(negz, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

        return cube;
    } 
    
    private void makeRGBTexture(GL gl, GLU glu, TextureReader.Texture img, int target, boolean mipmapped) {
        if (target == GL.GL_TEXTURE_2D) {
            if (mipmapped) {
                glu.gluBuild2DMipmaps(target, GL.GL_RGB8, img.getWidth(), img.getHeight(), GL.GL_RGB, GL.GL_UNSIGNED_BYTE, img.getPixels());
            } else {
                gl.glTexImage2D(target, 0, GL.GL_RGB, img.getWidth(), img.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, img.getPixels());
            }
        } else {
            gl.glTexImage2D(target, 0, GL.GL_RGBA, img.getWidth(), img.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, img.getPixels());
        }
    }
    
    private void loadTexture(String name, int target, int id) {
        if (target == GL.GL_TEXTURE_2D) {
//            gl.glBindTexture(target, texture[id]);
        } else //cube map
        {
  //          gl.glBindTexture(target, cubetexture[id]);
        }
        TextureReader.Texture textureRead = null;
        try {
            textureRead = TextureReader.readTexture(name);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        makeRGBTexture(gl, glu, textureRead, target, false);

        if (target == GL.GL_TEXTURE_2D) {
            float maxAnisotropy[] = new float[1];
            gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
            if (maxAnisotropy[0] == 16.0f) {
                gl.glTexParameteri(target, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
            }

            gl.glTexParameteri(target, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        }
    }

    public Texture loadTextureFromFile (String fileName) throws IOException{
        File file = new File(fileName);
        try{
            Texture t = TextureIO.newTexture(file, true);
            return t;
        } catch(IOException e){
            throw e;
        }
    }

    int cubeMap;
	         
    public void loadAllTexture() throws IOException{
            cube = loadCubeTextFromFile(cubeMapFileName);
            gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_S,
                    GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_T,
                    GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_R,
                    GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MAG_FILTER,
                    GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_MIN_FILTER,
                    GL.GL_NEAREST);
            gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
            gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
    }
    private void loadShaderFromFile (int shader, String fileName) throws IOException {
        try {
            BufferedReader brv = new BufferedReader(new FileReader(fileName));
            String vsrc = "";
            String line;
            while ((line = brv.readLine()) != null) {
                vsrc += line + "\n";
            }
            gl.glShaderSource(shader, 1, new String[]{vsrc}, (int[]) null, 0);
        } catch (IOException e) {
            throw e;
        }
        gl.glCompileShader(shader);
    }
    int shaderV, shaderF, reflectV, reflectF;
    public void loadAllShader() throws IOException {
        try{
            skyShaderV = gl.glCreateShader(GL.GL_VERTEX_SHADER_ARB);
            skyShaderF = gl.glCreateShader(GL.GL_FRAGMENT_SHADER_ARB);
            loadShaderFromFile(skyShaderV, "shader/sky.vert");
            loadShaderFromFile(skyShaderF, "shader/sky.frag");
            reflectV = gl.glCreateShader(GL.GL_VERTEX_SHADER_ARB);
            reflectF = gl.glCreateShader(GL.GL_FRAGMENT_SHADER_ARB);
            loadShaderFromFile(reflectV, "shader/reflection.vs");
            loadShaderFromFile(reflectF, "shader/reflection.frag");
        } catch (IOException e){
            throw e;
        }
    }
    public synchronized void keyPressed(KeyEvent e){
	if(e.getKeyCode() == KeyEvent.VK_UP){
     //       System.out.println("up key pressed " + alpha);
            if(alpha + 0.1f < Math.PI/2)
            alpha += 0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_DOWN){
            if(alpha - 0.1f > -Math.PI/2)
            alpha -= 0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            belta += 0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_LEFT){
            belta -=0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_A){ //left
            cameraPos[0] += 0.01f * Math.sin(belta);
            cameraPos[2] -= 0.01f * Math.cos(belta);
        }
	if(e.getKeyCode() == KeyEvent.VK_D){ //right
            cameraPos[0] -= 0.01f * Math.sin(belta);
            cameraPos[2] += 0.01f * Math.cos(belta);
	}
	if(e.getKeyCode() == KeyEvent.VK_W){ //front
            cameraPos[0] += 0.01f * Math.cos(belta);
            cameraPos[2] += 0.01f * Math.sin(belta);
	}
	if(e.getKeyCode() == KeyEvent.VK_S){ //back
            cameraPos[0] -= 0.01f * Math.cos(belta);
            cameraPos[2] -= 0.01f * Math.sin(belta);
	}
	if(e.getKeyCode() == KeyEvent.VK_PAGE_UP){
            cameraPos[1] += 1.0f;
	}
	if(e.getKeyCode() ==  KeyEvent.VK_PAGE_DOWN){
            cameraPos[1] -= 1.0f;
	}
	if(e.getKeyCode() == KeyEvent.VK_Q){
            System.out.println(reflectPrecent);
            if(reflectPrecent < 1.0f)
            reflectPrecent += 0.1f;
	}
	if(e.getKeyCode() == KeyEvent.VK_E){
            if(reflectPrecent > 0.0f)
                reflectPrecent -= 0.1f;
	}
		

	}

    public synchronized void keyReleased(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {

        }
    }
    public void keyTyped(KeyEvent e){
    }
}
