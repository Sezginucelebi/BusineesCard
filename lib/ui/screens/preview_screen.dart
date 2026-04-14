import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:share_plus/share_plus.dart';
import '../../models/business_card.dart';
import '../widgets/business_card_item.dart';

class PreviewScreen extends StatelessWidget {
  final BusinessCard card;

  const PreviewScreen({super.key, required this.card});

  @override
  Widget build(BuildContext context) {
    // vCard formatını oluştur (Rehbere eklemek için standart format)
    final String vCardData = """
BEGIN:VCARD
VERSION:3.0
FN:${card.name}
ORG:${card.company}
TITLE:${card.title}
ADR:;;${card.address};;;;
TEL:${card.phones.join(',')}
EMAIL:${card.email}
URL:${card.website}
END:VCARD
""";

    return Scaffold(
      appBar: AppBar(
        title: const Text("Kart Detayı"),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // Üstte kartın canlı ön izlemesi (Düzenle/Sil butonları burada gizli)
            BusinessCardItem(
              card: card,
              showActions: false,
            ),
            const SizedBox(height: 32),
            
            // QR Kod Kartı
            Card(
              elevation: 4,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 30, horizontal: 20),
                child: Column(
                  children: [
                    const Text(
                      "Paylaşım İçin Taratın",
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
                    ),
                    const SizedBox(height: 24),
                    // QR Kod üretimi
                    QrImageView(
                      data: vCardData,
                      version: QrVersions.auto,
                      size: 200.0,
                      backgroundColor: Colors.white,
                    ),
                    const SizedBox(height: 16),
                    Text(
                      "Kameranızı QR koda yaklaştırın",
                      style: TextStyle(color: Colors.grey.shade600, fontSize: 12),
                    ),
                  ],
                ),
              ),
            ),
            
            const SizedBox(height: 24),
            
            // NFC Bilgi Kutusu
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blueGrey.withOpacity(0.08),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.blueGrey.withOpacity(0.2)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.nfc, color: Colors.blueGrey, size: 30),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          "NFC Paylaşımı Aktif",
                          style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                        ),
                        Text(
                          "Telefonunuzu başka bir cihaza yaklaştırarak bilgilerinizi iletebilirsiniz.",
                          style: TextStyle(fontSize: 12, color: Colors.blueGrey.shade700),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 40),
          ],
        ),
      ),
      // Diğer paylaşım seçenekleri için buton
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          Share.share(vCardData, subject: '${card.name} Dijital Kartvizit');
        },
        label: const Text("Paylaş"),
        icon: const Icon(Icons.share),
      ),
    );
  }
}