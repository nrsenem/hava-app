# Hava — Şehir Arama ve Hava Tahmini

Bilişim Kampı Android eğitiminde geliştirilen küçük bir hava durumu uygulaması. [Open-Meteo](https://open-meteo.com/) servisinden şehir arar, seçilen şehrin anlık durumunu, saatlik ve 7 günlük tahminini gösterir. Yükleniyor / veri / boş / hata durumlarının hepsini ele alır.

**Teknoloji:** Kotlin · Jetpack Compose (Material 3) · ViewModel + StateFlow · Coroutines · Retrofit + Gson · Navigation Compose

## Özellikler

- Şehir arama (klavyedeki **Ara** tuşuyla)
- Arama sonucu yoksa "Şehir bulunamadı" mesajı
- Şehre dokununca anlık sıcaklık, hissedilen sıcaklık, nem ve rüzgâr
- Saatlik (24 saat) ve 7 günlük tahmin
- İnternet yoksa anlaşılır hata mesajı ve **Tekrar dene** düğmesi
- Ekran döndürüldüğünde seçili şehir kaybolmaz

## Mimari

Üç katman, tek `:app` modülü:

```
UI (Composable)  →  ViewModel (StateFlow)  →  Repository (suspend)  →  Retrofit → Open-Meteo
```

- **UI** yalnızca aldığı state'i çizer, olayları yukarı bildirir.
- **ViewModel** state'i yönetir; dört duruma çevirir.
- **Repository** veriye erişir; ekranı bilmez.

Her ekran dört durumu `LoadState` ile karşılar:

| Durum | Anlamı |
|---|---|
| `Loading` | Veri bekleniyor |
| `Success` | Veri geldi |
| `Empty` | Cevap geldi ama içi boş |
| `Error` | Bir şey ters gitti (mesaj + Tekrar dene) |

## Dosya yapısı

Tüm dosyalar `com.kampplus.hava` paketindedir.

| Dosya | Görevi |
|---|---|
| `MainActivity.kt` | Uygulamanın kapısı; `CityRoute` ve `CityScreen` (şehir arama ekranı) |
| `HavaHost.kt` | `HavaNavHost`: iki ekran arası geçiş |
| `CityCard.kt` | Listedeki tek şehir kartı |
| `City.kt` | `City` ve `CitySearchResponse` modelleri |
| `CityApi.kt` · `ApiClient.kt` | Retrofit arayüzleri ve kurulum |
| `CityRepository.kt` | Şehir arama |
| `CityViewModel.kt` | Arama ekranının state'i |
| `ForecastScreen.kt` | Tahmin ekranı (`ForecastRoute` + `ForecastScreen`) |
| `ForecastViewModel.kt` | Tahmin ekranının state'i |
| `ForecastApi.kt` · `ForecastResponse.kt` | Tahmin servisi ve sütun → satır dönüşümü |
| `WeatherRepository.kt` | Tahmin verisine erişim |
| `Forecast.kt` | Tahmin modelleri |
| `WeatherCondition.kt` | WMO hava kodu → emoji + Türkçe etiket |
| `LoadState.kt` | Dört durumlu `sealed interface` |

## API — Open-Meteo (anahtar gerekmez)

| Amaç | Çağrı |
|---|---|
| Şehir arama | `https://geocoding-api.open-meteo.com/v1/search?name=Ankara&count=10&language=tr` |
| Tahmin | `https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=..&hourly=..&daily=..&forecast_days=7&timezone=auto` |

- Sonuç yoksa arama yanıtında `results` alanı **hiç gelmez** → Boş (Empty) durumu.
- Tahmin yanıtı **sütun** biçimindedir: `hourly.time[i]` ile `hourly.temperature_2m[i]` aynı saate aittir.
- Ücretsiz kullanım ticari olmayan projeler içindir.

## Çalıştırma

1. Projeyi **Android Studio**'da aç ve Gradle sync'in bitmesini bekle.
2. Bir emülatör ya da USB ile bağlı telefon seç.
3. Yeşil **▶ Run** düğmesine bas.

Gereksinimler: Android Studio (güncel sürüm), JDK 17+ (Android Studio ile gelir), `minSdk 26`. Uygulama internet izni (`INTERNET`) ister.

Komut satırından:

```bash
./gradlew assembleDebug      # debug APK
```

Windows PowerShell'de `.\gradlew.bat assembleDebug`.

## Kamp adımları

| Uygulama | Konu |
|---|---|
| Uygulama 2 | İlk ekran: `CityCard`, `LazyColumn`, Logcat |
| Uygulama 3 | Canlanan ekran: arama kutusu, `rememberSaveable`, filtre, boş durum |
| Uygulama 4 | İnternete bağlan: Retrofit + Gson, `CityViewModel`, dört durum |
| Uygulama 5 | Tahmin ekranı ve Navigation Compose |

## Sık karşılaşılan sorunlar

- **Liste hiç gelmiyor:** `AndroidManifest.xml` içinde `INTERNET` izni olduğundan emin ol.
- **`Read timed out` ile Gradle build'i düşüyor:** Ağ yavaş olabilir. `gradle.properties` içinde zaman aşımını artır, yeniden sync yap.
- **`Unresolved reference`:** Bağımlılıklar için **File → Sync Project with Gradle Files** de.
