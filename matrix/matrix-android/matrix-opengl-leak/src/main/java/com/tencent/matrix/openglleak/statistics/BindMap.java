package com.tencent.matrix.openglleak.statistics;

import com.tencent.matrix.openglleak.statistics.resource.OpenGLInfo;
import com.tencent.matrix.openglleak.utils.ExecuteCenter;

import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
import static android.opengl.GLES30.GL_COPY_READ_BUFFER;
import static android.opengl.GLES30.GL_COPY_WRITE_BUFFER;
import static android.opengl.GLES30.GL_PIXEL_PACK_BUFFER;
import static android.opengl.GLES30.GL_PIXEL_UNPACK_BUFFER;
import static android.opengl.GLES30.GL_TEXTURE_2D_ARRAY;
import static android.opengl.GLES30.GL_TEXTURE_3D;
import static android.opengl.GLES30.GL_TRANSFORM_FEEDBACK_BUFFER;
import static android.opengl.GLES30.GL_UNIFORM_BUFFER;
import static javax.microedition.khronos.opengles.GL11ExtensionPack.GL_TEXTURE_CUBE_MAP;
import static javax.microedition.khronos.opengles.GL11ExtensionPack.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
import static javax.microedition.khronos.opengles.GL11ExtensionPack.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;

public class BindMap {

    private static final BindMap mInstance = new BindMap();

    private final Map<Long, Map<Integer, OpenGLInfo>> bindTextureMap;
    private final Map<Long, Map<Integer, OpenGLInfo>> bindBufferMap;
    private final Map<Long, Map<Integer, OpenGLInfo>> bindRenderbufferMap;

    static BindMap getInstance() {
        return mInstance;
    }

    private BindMap() {
        bindTextureMap = new HashMap<>();
        bindBufferMap = new HashMap<>();
        bindRenderbufferMap = new HashMap<>();
    }

    private OpenGLInfo getBindMapInfo(final Map<Long, Map<Integer, OpenGLInfo>> bindMap, long eglContextNativeHandle, int target) {
        synchronized (bindMap) {
            Map<Integer, OpenGLInfo> subTextureMap = bindMap.get(eglContextNativeHandle);
            if (subTextureMap == null) {
                subTextureMap = new HashMap<>();
                bindMap.put(eglContextNativeHandle, subTextureMap);
            }
            return subTextureMap.get(target);
        }
    }

    private void putInBindMap(final Map<Long, Map<Integer, OpenGLInfo>> bindMap, final int target, final long eglContextNativeHandle, final OpenGLInfo openGLInfo, OpenGLInfo.TYPE type) {
        if (!isSupportTarget(type, target)) {
            return;
        }
        synchronized (bindMap) {
            Map<Integer, OpenGLInfo> subTextureMap = bindMap.get(eglContextNativeHandle);
            if (subTextureMap == null) {
                subTextureMap = new HashMap<>();
                bindMap.put(eglContextNativeHandle, subTextureMap);
            }
            subTextureMap.put(target, openGLInfo);
        }
    }

    private boolean isSupportTarget(OpenGLInfo.TYPE type, int target) {
        if (type == OpenGLInfo.TYPE.TEXTURE) {
            return isSupportTargetOfTexture(target);
        }
        if (type == OpenGLInfo.TYPE.BUFFER) {
            return isSupportTargetOfBuffer(target);
        }
        if (type == OpenGLInfo.TYPE.RENDER_BUFFERS) {
            return isSupportTargetOfRenderbuffer(target);
        }
        return false;
    }

    private boolean isSupportTargetOfTexture(int target) {
        return target == GL_TEXTURE_2D || target == GL_TEXTURE_3D || target == GL_TEXTURE_CUBE_MAP_POSITIVE_X
                || target == GL_TEXTURE_CUBE_MAP_NEGATIVE_X || target == GL_TEXTURE_CUBE_MAP_POSITIVE_Y
                || target == GL_TEXTURE_CUBE_MAP_NEGATIVE_Y || target == GL_TEXTURE_CUBE_MAP_POSITIVE_Z
                || target == GL_TEXTURE_CUBE_MAP_NEGATIVE_Z || target == GL_TEXTURE_2D_ARRAY || target == GL_TEXTURE_CUBE_MAP;
    }

    private boolean isSupportTargetOfBuffer(int target) {
        return target == GL_ARRAY_BUFFER || target == GL_COPY_WRITE_BUFFER || target == GL_COPY_READ_BUFFER
                || target == GL_ELEMENT_ARRAY_BUFFER || target == GL_PIXEL_PACK_BUFFER || target == GL_PIXEL_UNPACK_BUFFER
                || target == GL_TRANSFORM_FEEDBACK_BUFFER || target == GL_UNIFORM_BUFFER;
    }

    private boolean isSupportTargetOfRenderbuffer(int target) {
        return target == GL_RENDERBUFFER;
    }

    public OpenGLInfo getBindInfo(OpenGLInfo.TYPE type, long eglContextId, int target) {
        switch (type) {
            case BUFFER:
                return getBindMapInfo(bindBufferMap, eglContextId, target);
            case TEXTURE:
                return getBindMapInfo(bindTextureMap, eglContextId, target);
            case RENDER_BUFFERS:
                return getBindMapInfo(bindRenderbufferMap, eglContextId, target);
        }
        return null;
    }


    public void putBindInfo(final OpenGLInfo.TYPE type, final int target, final long eglContextId, final OpenGLInfo info) {
        ExecuteCenter.getInstance().post(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case BUFFER:
                        putInBindMap(bindBufferMap, target, eglContextId, info, OpenGLInfo.TYPE.BUFFER);
                        break;
                    case TEXTURE:
                        putInBindMap(bindTextureMap, target, eglContextId, info, OpenGLInfo.TYPE.TEXTURE);
                        break;
                    case RENDER_BUFFERS:
                        putInBindMap(bindRenderbufferMap, target, eglContextId, info, OpenGLInfo.TYPE.RENDER_BUFFERS);
                        break;
                }
            }
        });
    }

}
