package com.ghosts.of.history.common.rendering

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.media.MediaPlayer
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import com.ghosts.of.history.common.rendering.ShaderUtil.checkGLError
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class VideoRenderer : OnFrameAvailableListener {
    private lateinit var videoTexture: SurfaceTexture
    private var mTextureId = 0
    private var mQuadProgram = 0
    private val lock = Object()
    private val modelViewProjection = FloatArray(16)
    private val modelView = FloatArray(16)
    private lateinit var mQuadVertices: FloatBuffer
    private val mModelMatrix = FloatArray(16)
    private lateinit var player: MediaPlayer
    private var frameAvailable = false
    private var done = false
    private var prepared = false
    var isStarted = false
        private set
    private lateinit var mTexCoordTransformationMatrix: Array<FloatArray>
    private var mQuadPositionParam = 0
    private var mQuadTexCoordParam = 0
    private var mModelViewProjectionUniform = 0
    private val VIDEO_QUAD_TEXTCOORDS_TRANSFORMED =
            floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f)

    fun createOnGlThread(context: Context) {
        // 1 texture to hold the video frame.
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureId = textures[0]
        val mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        GLES20.glBindTexture(mTextureTarget, mTextureId)
        GLES20.glTexParameteri(mTextureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(mTextureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        videoTexture = SurfaceTexture(mTextureId)
        videoTexture.setOnFrameAvailableListener(this)
        mTexCoordTransformationMatrix = Array(1) { FloatArray(16) }
        createQuardCoord()
        createQuadTextCoord()
        val vertexShader = ShaderUtil.loadGLShader(
                tag = TAG,
                context = context,
                type = GLES20.GL_VERTEX_SHADER,
                filename = VERTEX_SHADER_NAME)
        val fragmentShader = ShaderUtil.loadGLShader(
                tag = TAG,
                context = context,
                type = GLES20.GL_FRAGMENT_SHADER,
                filename = FRAGMENT_SHADER_NAME)
        mQuadProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mQuadProgram, vertexShader)
        GLES20.glAttachShader(mQuadProgram, fragmentShader)
        GLES20.glLinkProgram(mQuadProgram)
        GLES20.glUseProgram(mQuadProgram)
        checkGLError(TAG, "Program creation")
        mQuadPositionParam = GLES20.glGetAttribLocation(mQuadProgram, "a_Position")
        mQuadTexCoordParam = GLES20.glGetAttribLocation(mQuadProgram, "a_TexCoord")
        mModelViewProjectionUniform =
                GLES20.glGetUniformLocation(mQuadProgram, "u_ModelViewProjection")
        checkGLError(TAG, "Program parameters")
        Matrix.setIdentityM(mModelMatrix, 0)
        initializeMediaPlayer()
    }

    fun update(modelMatrix: FloatArray, scaleFactor: Float) {
        val scaleMatrix = FloatArray(16)
        Matrix.setIdentityM(scaleMatrix, 0)
        scaleMatrix[0] = scaleFactor
        scaleMatrix[5] = scaleFactor
        scaleMatrix[10] = scaleFactor
        Matrix.multiplyMM(mModelMatrix, 0, modelMatrix, 0, scaleMatrix, 0)
    }

    fun draw(cameraView: FloatArray, cameraPerspective: FloatArray) {
        if (done || !prepared) {
            return
        }
        synchronized(this) {
            if (frameAvailable) {
                videoTexture.updateTexImage()
                frameAvailable = false
                videoTexture.getTransformMatrix(mTexCoordTransformationMatrix[0])
                setVideoDimensions(mTexCoordTransformationMatrix[0])
                createQuadTextCoord()
            }
        }
        Matrix.multiplyMM(modelView, 0, cameraView, 0, mModelMatrix, 0)
        Matrix.multiplyMM(modelViewProjection, 0, cameraPerspective, 0, modelView, 0)

        GLES20.glEnable(GL10.GL_BLEND)
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        GLES20.glUseProgram(mQuadProgram)


        // Set the vertex positions.
        GLES20.glVertexAttribPointer(
                /* indx = */ mQuadPositionParam,
                /* size = */ COORDS_PER_VERTEX,
                /* type = */ GLES20.GL_FLOAT,
                /* normalized = */ false,
                /* stride = */ 0,
                /* ptr = */ mQuadVertices)

        // Set the texture coordinates.
        GLES20.glVertexAttribPointer(
                /* indx = */ mQuadTexCoordParam,
                /* size = */ TEXCOORDS_PER_VERTEX,
                /* type = */ GLES20.GL_FLOAT,
                /* normalized = */ false,
                /* stride = */ 0,
                /* ptr = */ fillBuffer(VIDEO_QUAD_TEXTCOORDS_TRANSFORMED))

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(mQuadPositionParam)
        GLES20.glEnableVertexAttribArray(mQuadTexCoordParam)
        GLES20.glUniformMatrix4fv(
                /* location = */ mModelViewProjectionUniform,
                /* count = */ 1,
                /* transpose = */ false,
                /* value = */ modelViewProjection,
                /* offset = */ 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(mQuadPositionParam)
        GLES20.glDisableVertexAttribArray(mQuadTexCoordParam)
        checkGLError(TAG, "Draw")
    }

    private fun setVideoDimensions(textureCoordMatrix: FloatArray) {
        var tempUVMultRes = uvMultMat4f(QUAD_TEXCOORDS[0], QUAD_TEXCOORDS[1], textureCoordMatrix)
        VIDEO_QUAD_TEXTCOORDS_TRANSFORMED[0] = tempUVMultRes[0]
        VIDEO_QUAD_TEXTCOORDS_TRANSFORMED[1] = tempUVMultRes[1]
        tempUVMultRes = uvMultMat4f(QUAD_TEXCOORDS[2], QUAD_TEXCOORDS[3], textureCoordMatrix)
        VIDEO_QUAD_TEXTCOORDS_TRANSFORMED[2] = tempUVMultRes[0]
        VIDEO_QUAD_TEXTCOORDS_TRANSFORMED[3] = tempUVMultRes[1]
        tempUVMultRes = uvMultMat4f(QUAD_TEXCOORDS[4], QUAD_TEXCOORDS[5], textureCoordMatrix)
        VIDEO_QUAD_TEXTCOORDS_TRANSFORMED[4] = tempUVMultRes[0]
        VIDEO_QUAD_TEXTCOORDS_TRANSFORMED[5] = tempUVMultRes[1]
        tempUVMultRes = uvMultMat4f(QUAD_TEXCOORDS[6], QUAD_TEXCOORDS[7], textureCoordMatrix)
        VIDEO_QUAD_TEXTCOORDS_TRANSFORMED[6] = tempUVMultRes[0]
        VIDEO_QUAD_TEXTCOORDS_TRANSFORMED[7] = tempUVMultRes[1]
    }

    fun play(filename: String, context: Context): Boolean {
        player.reset()
        done = false
        player.setOnPreparedListener { mp: MediaPlayer ->
            prepared = true
            mp.start()
        }
        player.setOnErrorListener { _: MediaPlayer?, _: Int, _: Int ->
            done = true
            false
        }
        player.setOnCompletionListener { done = true }
        player.setOnInfoListener { _: MediaPlayer?, _: Int, _: Int -> false }
        try {
            val assets = context.assets
            val descriptor = assets.openFd(filename)
            player.setDataSource(
                    descriptor.fileDescriptor,
                    descriptor.startOffset,
                    descriptor.length)
            player.setSurface(Surface(videoTexture))
            player.isLooping = true
            player.prepareAsync()
            synchronized(this) { isStarted = true }
        } catch (e: IOException) {
            Log.e(TAG, "Exception preparing movie", e)
            return false
        }
        return true
    }

    private fun initializeMediaPlayer() {
        val handler = handler ?: run {
            val newHandler = Handler(Looper.getMainLooper())
            handler = newHandler
            newHandler
        }
        handler.post {
            synchronized(lock) {
                player = MediaPlayer()
                lock.notify()
            }
        }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        synchronized(this) { frameAvailable = true }
    }

    private fun createQuadTextCoord() {
        val numVertices = 4
        val bbTexCoords =
                ByteBuffer.allocateDirect(numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE)
        bbTexCoords.order(ByteOrder.nativeOrder())
        val mQuadTexCoord = bbTexCoords.asFloatBuffer()
        mQuadTexCoord.put(QUAD_TEXCOORDS)
        mQuadTexCoord.position(0)
    }

    // Make a quad to hold the movie
    private fun createQuardCoord() {
        val bbVertices = ByteBuffer.allocateDirect(QUAD_COORDS.size * FLOAT_SIZE)
        bbVertices.order(ByteOrder.nativeOrder())
        mQuadVertices = bbVertices.asFloatBuffer()
        mQuadVertices.put(QUAD_COORDS)
        mQuadVertices.position(0)
    }

    private fun fillBuffer(array: FloatArray): Buffer {
        // Convert to floats because OpenGL doesnt work on doubles, and manually
        // casting each input value would take too much time.
        val bb = ByteBuffer.allocateDirect(4 * array.size) // each float takes 4 bytes
        bb.order(ByteOrder.LITTLE_ENDIAN)
        for (d in array) bb.putFloat(d)
        bb.rewind()
        return bb
    }

    private fun uvMultMat4f(u: Float,
                            v: Float,
                            pMat: FloatArray): FloatArray {
        val x = pMat[0] * u + pMat[4] * v + pMat[12] * 1f
        val y = pMat[1] * u + pMat[5] * v + pMat[13] * 1f
        return floatArrayOf(x, y)
    }

    companion object {
        private val TAG = VideoRenderer::class.java.simpleName

        private const val TEXCOORDS_PER_VERTEX = 2
        private const val COORDS_PER_VERTEX = 3
        private const val VERTEX_SHADER_NAME = "shaders/video.vert"
        private const val FRAGMENT_SHADER_NAME = "shaders/video.frag"
        private val QUAD_COORDS = floatArrayOf(
                -1.0f, -1.0f, 0.0f,
                -1.0f, +1.0f, 0.0f,
                +1.0f, -1.0f, 0.0f,
                +1.0f, +1.0f, 0.0f)
        private val QUAD_TEXCOORDS = floatArrayOf(
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f)
        private const val FLOAT_SIZE = 4
        private var handler: Handler? = null
    }
}