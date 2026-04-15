import 'package:flutter/material.dart';
import '../services/auth_service.dart';

class StoreView extends StatefulWidget {
  const StoreView({super.key});

  @override
  State<StoreView> createState() => _StoreViewState();
}

class _StoreViewState extends State<StoreView> {
  final AuthService _authService = AuthService();
  SubscriptionLevel _currentLevel = SubscriptionLevel.none;

  @override
  void initState() {
    super.initState();
    _loadCurrentLevel();
  }

  Future<void> _loadCurrentLevel() async {
    final level = await _authService.getSubscriptionLevel();
    setState(() => _currentLevel = level);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Premium Mağaza"),
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.black,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            const Icon(Icons.stars, size: 80, color: Colors.amber),
            const SizedBox(height: 10),
            const Text(
              "Premium'a Geçin",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const Text(
              "Daha fazla kart oluşturun ve profesyonel kalın.",
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 30),

            // Aylık Paket
            _buildStoreCard(
              title: "Aylık Paket",
              price: "₺29.99 / Ay",
              description: "1 + 1 (Toplam 2) Kart Oluşturma",
              level: SubscriptionLevel.monthly,
              color: Colors.blue.shade50,
              icon: Icons.calendar_month,
            ),

            const SizedBox(height: 15),

            // Yıllık Paket
            _buildStoreCard(
              title: "Yıllık Paket",
              price: "₺249.99 / Yıl",
              description: "10 Kart Oluşturma Kapasitesi",
              level: SubscriptionLevel.yearly,
              color: Colors.amber.shade50,
              icon: Icons.workspace_premium,
              isBestValue: true,
            ),

            const SizedBox(height: 40),
            const Text(
              "Ödemeler Google Play / App Store üzerinden güvenle yapılır.",
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStoreCard({
    required String title,
    required String price,
    required String description,
    required SubscriptionLevel level,
    required Color color,
    required IconData icon,
    bool isBestValue = false,
  }) {
    bool isCurrent = _currentLevel == level;

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: isBestValue ? Colors.amber : Colors.blue.shade200,
          width: 2,
        ),
      ),
      child: Column(
        children: [
          if (isBestValue)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(color: Colors.amber, borderRadius: BorderRadius.circular(10)),
              child: const Text("EN POPÜLER", style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold)),
            ),
          const SizedBox(height: 10),
          Icon(icon, size: 40, color: isBestValue ? Colors.amber.shade800 : Colors.blue.shade800),
          const SizedBox(height: 10),
          Text(title, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          Text(price, style: const TextStyle(fontSize: 18, color: Colors.black87)),
          const SizedBox(height: 5),
          Text(description, style: const TextStyle(color: Colors.black54)),
          const SizedBox(height: 20),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: isCurrent ? null : () async {
                await _authService.upgradeSubscription(level);
                setState(() => _currentLevel = level);
                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text("$title Aktif Edildi!")),
                  );
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: isBestValue ? Colors.amber : Colors.blue,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              child: Text(
                isCurrent ? "Mevcut Paket" : "Şimdi Satın Al",
                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
