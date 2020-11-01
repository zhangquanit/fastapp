package com.android.base.util.crash;

import android.os.Handler;
import android.os.Looper;import com.example.crash_handler.MainQuitException;

public final class AppCrashHandler {

    public interface ExceptionHandler {

        void handlerException(Thread thread, Throwable throwable);
    }

    private AppCrashHandler() {
    }

    private static ExceptionHandler sExceptionHandler;
    private static Thread.UncaughtExceptionHandler sUncaughtExceptionHandler;
    private static boolean sInstalled = false;//标记位，避免重复安装卸载

    /**
     * 当主线程或子线程抛出异常时会调用exceptionHandler.handlerException(Thread thread, Throwable throwable)
     * <p>
     * exceptionHandler.handlerException可能运行在非UI线程中。
     * <p>
     * 若设置了Thread.setDefaultUncaughtExceptionHandler则可能无法捕获子线程异常。
     *
     * @param exceptionHandler
     */
    public static synchronized void install(ExceptionHandler exceptionHandler) {
        if (sInstalled) {
            return;
        }
        sInstalled = true;
        sExceptionHandler = exceptionHandler;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        //之前的loop已经在while(true)停住了，所以这里继续调用loop()循环处理消息队列
                        Looper.loop();
                    } catch (Throwable e) {
                        if (e instanceof MainQuitException) {
                            return;
                        }
                        //主线程异常
                        if (sExceptionHandler != null) {
                            sExceptionHandler.handlerException(Looper.getMainLooper().getThread(), e);
                        }
                    }
                }
            }
        });

        sUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        //捕获子线程异常
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (sExceptionHandler != null) {
                    sExceptionHandler.handlerException(t, e);
                }

                //如果有第三方需要处理exception
//                sUncaughtExceptionHandler.uncaughtException(t, e);
            }
        });

    }

    public static synchronized void uninstall() {
        if (!sInstalled) {
            return;
        }
        sInstalled = false;
        sExceptionHandler = null;
        //卸载后恢复默认的异常处理逻辑，否则主线程再次抛出异常后将导致ANR，并且无法捕获到异常位置
        Thread.setDefaultUncaughtExceptionHandler(sUncaughtExceptionHandler);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                throw new MainQuitException("Quit Cockroach.....");//主线程抛出异常，迫使 while (true) {}结束
            }
        });

    }
}