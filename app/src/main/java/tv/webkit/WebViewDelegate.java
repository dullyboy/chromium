/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.webkit;

import tv.annotation.SystemApi;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.IWallpaperManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.net.http.ErrorStrings;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.SparseArray;
import android.view.HardwareCanvas;
import android.view.View;
import android.view.ViewRootImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//import java.lang.reflect.Method;

/**
 * Delegate used by the WebView provider implementation to access
 * the required framework functionality needed to implement a {@link WebView}.
 *
 * @hide
 */
@SystemApi
public final class WebViewDelegate {

    /* package */ WebViewDelegate() { }

    //liubin add start
    public static String TAG = "WebViewDelegate";

    public static final long TRACE_TAG_WEBVIEW = 1L << 4;
    private static Method mGetLoadedPackageInfoMethod = null;
    private static Method mAddAssetPathMethod = null;
    private static Method mGetViewRootImplMethod = null;
    private static Method mInvokeFunctorMethod =null;
    private static Method mCallDrawGLFunctionMethod = null;
    private static Method mDetachFunctorMethod = null;
    private static Method mGetAssignedPackageIdentifiersMethod = null;
    static {
            try {
                mGetLoadedPackageInfoMethod = Class.forName("tv.webkit.WebViewFactory")
                        .getMethod("getLoadedPackageInfo");
                mAddAssetPathMethod = Class.forName("android.content.res.AssetManager")
                        .getMethod("addAssetPath", String.class);
                mGetViewRootImplMethod = View.class.getMethod("getViewRootImpl");
                mInvokeFunctorMethod = Class.forName("android.view.ViewRootImpl")
                        .getMethod("invokeFunctor", long.class, boolean.class);
                mCallDrawGLFunctionMethod = Class.forName("android.view.HardwareCanvas")
                        .getMethod("callDrawGLFunction2", long.class);
                mDetachFunctorMethod = Class.forName("android.view.ViewRootImpl")
                        .getMethod("detachFunctor", long.class);
                mGetAssignedPackageIdentifiersMethod = AssetManager.class
                        .getMethod("getAssignedPackageIdentifiers");
            } catch (Exception e) {
                throw new RuntimeException("Invalid reflection", e);
            }
    }
    //liubin add end

    /**
     * Listener that gets notified whenever tracing has been enabled/disabled.
     */
    public interface OnTraceEnabledChangeListener {
        void onTraceEnabledChange(boolean enabled);
    }

    /**
     * Register a callback to be invoked when tracing for the WebView component has been
     * enabled/disabled.
     */
    public void setOnTraceEnabledChangeListener(final OnTraceEnabledChangeListener listener) {
        SystemProperties.addChangeCallback(new Runnable() {
            @Override
            public void run() {
                listener.onTraceEnabledChange(isTraceTagEnabled());
            }
        });
    }

    /**
     * Returns true if the WebView trace tag is enabled and false otherwise.
     */
    public boolean isTraceTagEnabled() {
        return false;
//        return Trace.isTagEnabled(/*Trace.*/TRACE_TAG_WEBVIEW);
    }

    /**
     * Returns true if the draw GL functor can be invoked (see {@link #invokeDrawGlFunctor})
     * and false otherwise.
     */
    public boolean canInvokeDrawGlFunctor(View containerView) {

//        ViewRootImpl viewRootImpl = containerView.getViewRootImpl();
         // viewRootImpl can be null during teardown when window is leaked.
//        return viewRootImpl != null;
        try {
            Object viewRootImpl = mGetViewRootImplMethod.invoke(containerView);
            // viewRootImpl can be null during teardown when window is leaked.
            return viewRootImpl != null;
        } catch (Exception e) {
            throw new RuntimeException("Invalid reflection", e);
        }
    }

    /**
     * Invokes the draw GL functor. If waitForCompletion is false the functor
     * may be invoked asynchronously.
     *
     * @param nativeDrawGLFunctor the pointer to the native functor that implements
     *        system/core/include/utils/Functor.h
     */
    public void invokeDrawGlFunctor(View containerView, long nativeDrawGLFunctor,
            boolean waitForCompletion) {
//        ViewRootImpl viewRootImpl = containerView.getViewRootImpl();
//        viewRootImpl.invokeFunctor(nativeDrawGLFunctor, waitForCompletion);
        try {
            Object viewRootImpl = mGetViewRootImplMethod.invoke(containerView);
            if (viewRootImpl != null) {
                mInvokeFunctorMethod.invoke(viewRootImpl, nativeDrawGLFunctor, waitForCompletion);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid reflection", e);
        }
    }

    /**
     * Calls the function specified with the nativeDrawGLFunctor functor pointer. This
     * functionality is used by the WebView for calling into their renderer from the
     * framework display lists.
     *
     * @param canvas a hardware accelerated canvas (see {@link Canvas#isHardwareAccelerated()})
     * @param nativeDrawGLFunctor the pointer to the native functor that implements
     *        system/core/include/utils/Functor.h
     * @throws IllegalArgumentException if the canvas is not hardware accelerated
     */
    public void callDrawGlFunction(Canvas canvas, long nativeDrawGLFunctor) {
//        if (!(canvas instanceof HardwareCanvas)) {
            // Canvas#isHardwareAccelerated() is only true for subclasses of HardwareCanvas.
//            throw new IllegalArgumentException(canvas.getClass().getName()
//                    + " is not hardware accelerated");
//        }
//        ((HardwareCanvas) canvas).callDrawGLFunction2(nativeDrawGLFunctor);
        try {
            mCallDrawGLFunctionMethod.invoke(canvas, nativeDrawGLFunctor);
        } catch (Exception e) {
            throw new RuntimeException("Invalid reflection", e);
        }
    }

    /**
     * Detaches the draw GL functor.
     *
     * @param nativeDrawGLFunctor the pointer to the native functor that implements
     *        system/core/include/utils/Functor.h
     */
    public void detachDrawGlFunctor(View containerView, long nativeDrawGLFunctor) {
//        ViewRootImpl viewRootImpl = containerView.getViewRootImpl();
//        if (nativeDrawGLFunctor != 0 && viewRootImpl != null) {
//            viewRootImpl.detachFunctor(nativeDrawGLFunctor);
//        }
        try {
            Object viewRootImpl = mGetViewRootImplMethod.invoke(containerView);
            if (viewRootImpl != null) {
                mDetachFunctorMethod.invoke(viewRootImpl, nativeDrawGLFunctor);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid reflection", e);
        }
    }

    /**
     * Returns the package id of the given {@code packageName}.
     */
    public int getPackageId(Resources resources, String packageName) {

//        SparseArray<String> packageIdentifiers =
//                resources.getAssets().getAssignedPackageIdentifiers();
//        for (int i = 0; i < packageIdentifiers.size(); i++) {
//            final String name = packageIdentifiers.valueAt(i);
//
//            if (packageName.equals(name)) {
//                return packageIdentifiers.keyAt(i);
//            }
//        }
//        throw new RuntimeException("Package not found: " + packageName);

        try {
            SparseArray packageIdentifiers =
                    (SparseArray) mGetAssignedPackageIdentifiersMethod.invoke(
                            resources.getAssets());
            for (int i = 0; i < packageIdentifiers.size(); i++) {
                final String name = (String) packageIdentifiers.valueAt(i);

                if (packageName.equals(name)) {
                    return packageIdentifiers.keyAt(i);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid reflection", e);
        }
        throw new RuntimeException("Package not found: " + packageName);

    }

    /**
     * Returns the application which is embedding the WebView.
     */
    public Application getApplication() {
        Log.d(TAG, "getApplication: " + ActivityThread.currentApplication());
        return ActivityThread.currentApplication();
    }

    /**
     * Returns the error string for the given {@code errorCode}.
     */
    public String getErrorString(Context context, int errorCode) {
        return ErrorStrings.getString(errorCode, context);
    }

    /**
     * Adds the WebView asset path to {@link AssetManager}.
     */

    public void addWebViewAssetPath(Context context) {
//        context.getAssets().addAssetPathes(
//                tv.webkit.WebViewFactory.getLoadedPackageInfo().applicationInfo.sourceDir);
    }
}
