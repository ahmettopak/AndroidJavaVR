package com.ahmet.androidjavavr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLView;
    MyGLRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        // OpenGL context oluştur
        mGLView = findViewById(R.id.leftView);

        // OpenGL versiyonunu ayarla
        mGLView.setEGLContextClientVersion(2);

        // Renderer ayarla
        mRenderer = new MyGLRenderer(this);

        mGLView.setRenderer(mRenderer);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }


}

class MyGLRenderer implements GLSurfaceView.Renderer {

    private final Context context;
    private int program;

    private static final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 texCoord;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "  vTexCoord = texCoord;" +
                    "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec2 vTexCoord;" +
                    "uniform sampler2D texture;" +
                    "uniform vec2 resolution;" +
                    "void main() {" +
                    "  vec2 uv = vTexCoord;" +
                    "  vec2 center = vec2(0.5, 0.5);" +
                    "  vec2 diff = uv - center;" +
                    "  float dist = length(diff);" +
                    "  float distSquared = dist * dist;" +
                    "  uv = center + diff * (1.0 + distSquared * 0.2);" + // Barrel distortion equation
                    "  if(uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {" +
                    "      gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" + // Black out-of-bounds pixels
                    "  } else {" +
                    "      gl_FragColor = texture2D(texture, uv);" +
                    "  }" +
                    "}";
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private final int vertexCount = 4;
    private final int vertexStride = 4 * 4;

    // Vertices of a fullscreen quad
    private final float[] vertices = {
            -1.0f, 1.0f, 0.0f,  // top left
            -1.0f, -1.0f, 0.0f,  // bottom left
            1.0f, -1.0f, 0.0f,  // bottom right
            1.0f, 1.0f, 0.0f  // top right
    };

    // Texture coordinates of the fullscreen quad
    private final float[] texCoords = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    private int positionHandle;
    private int texCoordHandle;
    private int textureUniformHandle;
    private int resolutionUniformHandle;

    private int texture;

    public MyGLRenderer(Context context) {
        this.context = context;

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer = tb.asFloatBuffer();
        textureBuffer.put(texCoords);
        textureBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        texCoordHandle = GLES20.glGetAttribLocation(program, "texCoord");
        textureUniformHandle = GLES20.glGetUniformLocation(program, "texture");
        resolutionUniformHandle = GLES20.glGetUniformLocation(program, "resolution");

        // Load the texture
        texture = TextureHelper.loadTexture(context, R.drawable.lake); // Replace 'your_image' with your image resource
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(program);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, textureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(textureUniformHandle, 0);

        GLES20.glUniform2f(resolutionUniformHandle, 1.0f, 1.0f); // Set resolution to 1x1 for simplicity

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}