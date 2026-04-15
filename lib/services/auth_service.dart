import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum SubscriptionLevel { none, monthly, yearly }

class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final GoogleSignIn _googleSignIn = GoogleSignIn();

  // Giriş yap (E-posta/Şifre)
  Future<User?> signInWithEmail(String email, String password) async {
    try {
      UserCredential result = await _auth.signInWithEmailAndPassword(
          email: email, password: password);
      return result.user;
    } catch (e) {
      rethrow;
    }
  }

  // Kayıt ol (E-posta/Şifre)
  Future<User?> registerWithEmail(String email, String password) async {
    try {
      UserCredential result = await _auth.createUserWithEmailAndPassword(
          email: email, password: password);
      return result.user;
    } catch (e) {
      rethrow;
    }
  }

  // Şifre Sıfırlama
  Future<void> resetPassword(String email) async {
    try {
      await _auth.sendPasswordResetEmail(email: email);
    } catch (e) {
      rethrow;
    }
  }

  // Google ile Giriş
  Future<User?> signInWithGoogle() async {
    try {
      final GoogleSignInAccount? googleUser = await _googleSignIn.signIn();
      if (googleUser == null) return null;

      final GoogleSignInAuthentication googleAuth = await googleUser.authentication;
      final AuthCredential credential = GoogleAuthProvider.credential(
        accessToken: googleAuth.accessToken,
        idToken: googleAuth.idToken,
      );

      UserCredential result = await _auth.signInWithCredential(credential);
      return result.user;
    } catch (e) {
      rethrow;
    }
  }

  // Çıkış yap
  Future<void> signOut() async {
    await _googleSignIn.signOut();
    await _auth.signOut();
  }

  // Mevcut kullanıcıyı al
  User? get currentUser => _auth.currentUser;

  // Abonelik Seviyesini Al
  Future<SubscriptionLevel> getSubscriptionLevel() async {
    final prefs = await SharedPreferences.getInstance();
    String? level = prefs.getString('sub_level');
    if (level == 'monthly') return SubscriptionLevel.monthly;
    if (level == 'yearly') return SubscriptionLevel.yearly;
    return SubscriptionLevel.none;
  }

  // Kart Limitini Hesapla
  Future<int> getCardLimit() async {
    final level = await getSubscriptionLevel();
    switch (level) {
      case SubscriptionLevel.monthly:
        return 2; // Aylık: 1 + 1 (Toplam 2)
      case SubscriptionLevel.yearly:
        return 10; // Yıllık: 10 kart
      case SubscriptionLevel.none:
      default:
        return 1; // Demo: 1 kart
    }
  }

  // Premium Satın Al (Simülasyon)
  Future<void> upgradeSubscription(SubscriptionLevel level) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('sub_level', level.name);
    await prefs.setBool('is_premium', level != SubscriptionLevel.none);
  }

  // Sadece isPremiumUser metodu eski kodlarla uyumluluk için kalsın
  Future<bool> isPremiumUser() async {
    final level = await getSubscriptionLevel();
    return level != SubscriptionLevel.none;
  }
}
