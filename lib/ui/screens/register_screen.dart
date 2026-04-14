import 'package:flutter/material.dart';
import 'login_screen.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  bool _isPasswordVisible = false;

  void _register() {
    if (_formKey.currentState!.validate()) {
      // Şimdilik sadece başarılı mesajı gösterip Login'e yönlendiriyoruz
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Kayıt başarılı! Giriş yapabilirsiniz.")),
      );
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => const LoginScreen()),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Yeni Hesap Oluştur"),
        backgroundColor: Colors.transparent,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(32),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              const Icon(Icons.person_add_outlined, size: 80, color: Colors.blueGrey),
              const SizedBox(height: 32),

              // Ad Soyad
              TextFormField(
                controller: _nameController,
                decoration: InputDecoration(
                  labelText: "Ad Soyad",
                  contentPadding: const EdgeInsets.symmetric(vertical: 20, horizontal: 16),
                  prefixIcon: const Icon(Icons.person),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                ),
                validator: (val) => val!.isEmpty ? "Lütfen adınızı girin" : null,
              ),
              const SizedBox(height: 20),

              // E-posta
              TextFormField(
                controller: _emailController,
                keyboardType: TextInputType.emailAddress,
                decoration: InputDecoration(
                  labelText: "E-posta",
                  contentPadding: const EdgeInsets.symmetric(vertical: 20, horizontal: 16),
                  prefixIcon: const Icon(Icons.email),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                ),
                validator: (val) => !val!.contains("@") ? "Geçerli bir e-posta girin" : null,
              ),
              const SizedBox(height: 20),

              // Şifre
              TextFormField(
                controller: _passwordController,
                obscureText: !_isPasswordVisible,
                decoration: InputDecoration(
                  labelText: "Şifre",
                  contentPadding: const EdgeInsets.symmetric(vertical: 20, horizontal: 16),
                  prefixIcon: const Icon(Icons.lock),
                  suffixIcon: IconButton(
                    icon: Icon(_isPasswordVisible ? Icons.visibility : Icons.visibility_off),
                    onPressed: () => setState(() => _isPasswordVisible = !_isPasswordVisible),
                  ),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                ),
                validator: (val) => val!.length < 6 ? "Şifre en az 6 karakter olmalı" : null,
              ),
              const SizedBox(height: 32),

              // Kayıt Butonu
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton(
                  onPressed: _register,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blueGrey,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                  child: const Text("Kayıt Ol", style: TextStyle(fontSize: 16)),
                ),
              ),

              const SizedBox(height: 16),

              // Giriş'e Geri Dön
              TextButton(
                onPressed: () {
                  Navigator.pop(context);
                },
                child: const Text("Zaten hesabınız var mı? Giriş Yapın"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}