-dontwarn javax.annotation.Nullable # почему то обфускатор кидает варн на эту аннотацию, поэтому указываем явно, что не нужно варить конкретный класс
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-keep public class com.android.installreferrer.** { *; }
-keep class com.progun.dunta_sdk.BaseDuntaAppCompatActivity { *; }
-keep public abstract class com.progun.dunta_sdk.api.** { *; }
-keepnames public abstract class com.progun.dunta_sdk.api.DuntaManager { *; }
-keep public abstract class com.progun.dunta_sdk.api.DuntaManager { *; }
-keep public enum com.progun.dunta_sdk.api.DuntaManager$ProxyState { *; }
-keep public interface com.progun.dunta_sdk.api.DuntaManager$OnUserChoiceClickListener { *; }
-keepnames class com.progun.dunta_sdk.api.DuntaManagerImpl { *; } #сохраняет имена всего внутри указанного класса

-keepattributes Signature, MethodParameters, Exceptions, LineNumberTable

-keepparameternames
-keeppackagenames com.progun.dunta_sdk.api.* #сохраняем имена указанных пакетов. исключается из обфускации

-verbose

#Обфускатор удаляет логи при сборке модуля
-assumenosideeffects class android.util.Log {
  public static int v(...);
  public static int d(...);
  public static int i(...);
  public static int w(...);
  public static int e(...);
}

#Обфускатор удаляет логи при сборке модуля
-assumenosideeffects class com.progun.dunta_sdk.utils.LogWrap {
  public static void v(...);
  public static void d(...);
  public static void i(...);
  public static void w(...);
  public static void e(...);
}