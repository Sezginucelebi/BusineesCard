# TODO

## 2026-04-20

### Tamamlanan duzenlemeler

- Android `CardEditorScreen.kt` icin eksik `Delete` icon importu eklendi.
- Android kok `build.gradle.kts` sadeleştirildi; plugin surum tekrarları temizlenerek plugin tanimlari tek kaynaga indirildi.
- Android debug derlemesi `./gradlew assembleDebug` ile tekrar calistirildi ve basarili dogrulandi.
- iOS icin `RootView.swift` eklendi; giris durumuna gore `LoginView` veya `CardListView` aciliyor.
- iOS `AppDelegate.swift` guncellendi; uygulama acilisinda `UIHostingController` ile SwiftUI root view baglandi.
- iOS `Info.plist` icinden `UIMainStoryboardFile` kaldirildi; acilis artik storyboard yerine SwiftUI root view ile yapiliyor.
- iOS `AuthService.swift` icine `Combine` importu eklendi.
- iOS `Helpers.swift` icine eksik `ColorPickerRow` bileseni eklendi.
- iOS `CardListView.swift` yeniden duzenlendi.
- iOS `CardListView.swift` icinde `signOut()` cagrisinin hata yakalama akisi duzeltildi.
- iOS `CardListView.swift` icinde magazayi acan sheet akisi eklendi.
- iOS `CardListView.swift` icinde kart detayina gecis icin `NavigationLink` akisi duzeltildi.
- iOS `CardDetailsView.swift` bozuk fonksiyon blogu temizlenerek yeniden kuruldu.
- iOS `CardDetailsView.swift` icindeki paylasim aksiyonu `shareAsImage()` olarak netlestirildi.
- iOS `CardDetailsView.swift` icinde secilen kart sabloni kaydedilirken `DatabaseService.shared.updateCard(...)` ile persist akisi eklendi.
- iOS `CardDetailsView.swift` icinden eksik assete bagli `apple_wallet_icon` kullanan gecici buton kaldirildi.
- Android ve iOS kart detay ekranlarinda QR kod boyutu buyutuldu; beyaz alan icini daha fazla dolduracak sekilde ic bosluk azaltildi.
- Android kart detay ekraninda ayri Google Wallet butonu kaldirildi; `Paylas` butonu altina `Resim olarak paylas` ve `Google Wallet'a ekle` secenekleri tasindi.
- Android `CardDetailsScreen.kt` icinde NFC durum gostergesi dinamik hale getirildi; cihazda NFC aciksa yesil `NFC Aktif`, kapaliysa kirmizi `NFC Kapali`, destek yoksa `NFC Desteklenmiyor` gosteriliyor.
- Android `CardDetailsScreen.kt` icindeki NFC durumu sistem yayinini dinleyecek sekilde guncellendi; ekran acikken NFC ayari degistiginde yazi ve renk anlik yenileniyor.
- Android QR kod uretiminde `MARGIN=0` kullanildi ve kart detay ekranindaki beyaz kutu buyutulup ic bosluk sifirlandi; QR kod kutuyu daha dolu kapliyor.
- Android `CardDetailsScreen.kt` icinde QR kod alanı tekrar duzenlenerek beyaz kutu `250`, QR kod ise `240` olarak guncellendi.
- Android `CardDetailsScreen.kt` icinde paylasim gorselindeki QR konumu merkezlenerek kayma giderildi; QR beyaz kutunun icinde simetrik oturuyor.
- Android debug APK icin `renameDebugApk` gorevi eklendi; derleme sonrasi `BusineesCard.apk` dosyasi `android/app/build/outputs/apk/renamed/` altinda uretiliyor.
- Android icin `renamedDebug` alias gorevi eklendi; task adi karisikliginda da ayni APK uretim akisi calisiyor.
- Android uygulama ikonu `assets/images/logo.png` kaynagi kullanilarak yeniden uretildi; `launcher_icon`, `ic_launcher` ve adaptive `ic_launcher_foreground` dosyalari guncellendi.
- iOS `CardEditorView.swift` icinde `BusinessCard` olusturulurken eksik alanlar (`phones2`, `photoPath`, `templateId`) tamamlandi.
- iOS `Runner.xcodeproj/project.pbxproj` guncellendi; SwiftUI ekranlari, servisler, model ve helper dosyalari target sources icine eklendi.

### Dogrulama notlari

- Android tarafi Windows ortaminda derlendi ve debug build gecti.
- iOS tarafi bu ortamda gercek `xcodebuild` ile dogrulanamadi; sebep Windows uzerinde iOS SDK/Xcode bulunmamasi.

### Siradaki kontrol noktasi

- Mac ortaminda `xcodebuild` veya Xcode ile iOS debug build alinacak.
- iOS derleme sonucu geldikten sonra kalan compile veya signing hatalari bu dosyaya eklenecek.
- Alisveris magazasi tasarimi kontrol edilecek; fiyatlandirma ve paketler duzenlenecek.

## 2026-04-21

### Testler

- Android kart detay ekraninda NFC ac/kapa degisiminde status yazisinin ve renginin anlik degistigi elle test edilecek.
- Android kart detay ekranindaki kart onizlemesi ile paylasilan JPEG gorselinin kart oranlari aynilandi; onizleme `1050:600` oranina cekildi.
- Android `CardDetailsScreen.kt` icinde kart onizlemesi, paylasim gorseli ve QR iceriği ayni `CardPresentationData` kaynagindan besleniyor.

## 2026-04-26

### Tamamlanan duzenlemeler

- Android `CardStyle.kt` bastan yazildi; kart yerlesimi tek referans olcu sistemi (`1050x600`) uzerinden standartlastirildi.
- Android kart detay ekrani icindeki eski ayri kart bilgi blogu kaldirildi; gorunum `StandardBusinessCard` bilesenine baglandi.
- Android kart editor onizlemesi de ayni `StandardBusinessCard` bilesenine baglandi; editor ve detay ekrani tek kart yapisini kullanmaya basladi.
- Android kart gorunumu, paylasilan JPEG ve QR yerlesimi ayni koordinat ve olcu mantigina cekildi; isim, unvan, sirket, adres, iletisim ve QR konumlari tek kaynaktan yonetiliyor.
- Android QR beyaz kutusu korunarak ic QR boyutu ince ayarlandi; kutu ici doluluk orani tek standarda baglandi.
- Android telefon numaralari icin ortak formatlayici eklendi (`PhoneFormat.kt`); giris, kayit, onizleme ve detay ekranda `xxxx xxx xx xx` yapisi uygulanmaya baslandi.
- Android `CardDetailsScreen.kt` icinde eski kayitlar da ortak telefon formatina normalize edilecek sekilde duzenlendi.
- Android Google Wallet akisi yeniden ele alindi; sahte yonlendirme yerine gercek entegrasyon iskeleti hazirlandi.
- Android `build.gradle.kts` icine `com.google.android.gms:play-services-pay:16.5.0` bagimliligi eklendi.
- Android `GoogleWalletService.kt` eklendi; Wallet uygunluk kontrolu, pass JSON uretimi, `savePasses(...)` cagrisi ve sonuc yonetimi tek serviste toplandi.
- Android `MainActivity.kt` icine Google Wallet `onActivityResult` yonetimi eklendi.
- Android `CardDetailsScreen.kt` icinde Google Wallet secenegi cihaz ve ayar uygunluguna gore dinamik etiketlenecek sekilde guncellendi.
- Android `gradle.properties` icine `GOOGLE_WALLET_ISSUER_ID`, `GOOGLE_WALLET_ISSUER_EMAIL`, `GOOGLE_WALLET_CLASS_SUFFIX` ve `GOOGLE_WALLET_ISSUER_NAME` alanlari eklendi.
- Proje `README.md` icine Google Wallet kurulum notu ve gerekli issuer alanlari eklendi.

### Dogrulama notlari

- Bu ortamda Android derlemesi tekrar ucundan uca dogrulanamadi; Gradle wrapper lock ve cache erisim kisitlari nedeniyle tam compile calistirilamadi.
- Google Wallet akisinin gercek cihazda calismasi icin Google Wallet Business Console onboarding ve issuer bilgilerinin `android/gradle.properties` icine girilmesi gerekiyor.

### Siradaki kontrol noktasi

- `android/gradle.properties` icindeki Google Wallet issuer bilgileri doldurulacak.
- Google Wallet pass icerigi gercek cihazda test edilerek `savePasses(...)` sonucu dogrulanacak.
- Gerekirse Generic Pass gorunumu, metin modulleri ve link alanlari kartvizit senaryosuna gore daha da zenginlestirilecek.
