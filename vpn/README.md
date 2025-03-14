# 🛡️ VPN Модуль

## 📌 Описание

Модуль для подключения через **OpenVPN**. Библиотеки для разных архитектур процессоров находятся в папке `jniLibs`.

## ⚙️ Настройка Gradle

Для корректной работы с VPN библиотеками добавьте в `build.gradle`:

```gradle
android {
    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}