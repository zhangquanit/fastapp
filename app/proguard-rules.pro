
#保留所注解
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

-keepattributes Signature
-keepattributes *Annotation*
-keep class **.R$* {
     *;
}

#Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod

#################################### Glide 混淆配置
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.**{*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#################################### Glide webp 混淆配置
-keep public class com.bumptech.glide.integration.webp.WebpImage { *; }
-keep public class com.bumptech.glide.integration.webp.WebpFrame { *; }
-keep public class com.bumptech.glide.integration.webp.WebpBitmapFactory { *; }

#################################### Retrofit 混淆配置
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions

#################################### okhttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**

#################################### RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#################################### EventBus
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#################################### 极光魔链
-dontwarn cn.magicwindow.**
-keep class cn.magicwindow.** {*;}

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }


#################################### 阿里百川
-keepattributes Signature
-ignorewarnings
-keep class javax.ws.rs.** { *; }
-keep class com.alibaba.fastjson.** { *; }
-dontwarn com.alibaba.fastjson.**
-keep class sun.misc.Unsafe { *; }
-dontwarn sun.misc.**
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**
-keep class org.json.** {*;}
-keep class com.ali.auth.**  {*;}
-dontwarn com.ali.auth.**
-keep class com.taobao.securityjni.** {*;}
-keep class com.taobao.wireless.security.** {*;}
-keep class com.taobao.dp.**{*;}
-keep class com.alibaba.wireless.security.**{*;}
-keep interface mtopsdk.mtop.global.init.IMtopInitTask {*;}
-keep class * implements mtopsdk.mtop.global.init.IMtopInitTask {*;}

#################################### 京东
-keep class com.kepler.**{*;}
-dontwarn com.kepler.**

#################################### 腾讯bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}


#################################### App 混淆配置
#Gson实体需要添加@Keep注解