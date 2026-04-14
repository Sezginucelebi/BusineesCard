import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'ui/screens/login_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  try {
    // Firebase'i başlatmayı deniyoruz, 5 saniye içinde cevap alamazsak devam et diyoruz
    await Firebase.initializeApp().timeout(const Duration(seconds: 5));
    print("Firebase başarıyla başlatıldı.");
  } catch (e) {
    print("Firebase başlatılamadı, ama uygulama açılıyor: $e");
    // Firebase başlamasa bile uygulamanın çökmesini engellemek için devam ediyoruz
  }

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'BusineesCard',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blueGrey),
        textTheme: GoogleFonts.poppinsTextTheme(),
      ),
      home: const LoginScreen(), 
    );
  }
}
