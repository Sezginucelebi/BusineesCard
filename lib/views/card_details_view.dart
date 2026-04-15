import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:screenshot/screenshot.dart';
import 'package:share_plus/share_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'package:nfc_manager/nfc_manager.dart';
import 'dart:io';
import '../models/business_card.dart';
import '../services/database_service.dart';
import 'card_editor_view.dart';

class CardDetailsView extends StatefulWidget {
  final BusinessCard card;
  const CardDetailsView({super.key, required this.card});

  @override
  State<CardDetailsView> createState() => _CardDetailsViewState();
}

class _CardDetailsViewState extends State<CardDetailsView> {
  final ScreenshotController _screenshotController = ScreenshotController();
  final DatabaseService _dbService = DatabaseService();
  late BusinessCard _currentCard;
  bool _isNfcAvailable = false;

  @override
  void initState() {
    super.initState();
    _currentCard = widget.card;
    _checkNfcAvailability();
  }

  Future<void> _checkNfcAvailability() async {
    bool isAvailable = await NfcManager.instance.isAvailable();
    setState(() {
      _isNfcAvailable = isAvailable;
    });
  }

  void _shareCardImage() async {
    final directory = (await getApplicationDocumentsDirectory()).path;
    String fileName = "BusineesCard_${DateTime.now().millisecondsSinceEpoch}.png";

    // Screenshot alırken ölçeklendirmeyi artırarak daha kaliteli (keskin) bir resim elde edebiliriz
    final imageBytes = await _screenshotController.capture(
      pixelRatio: 3.0, // Daha yüksek kalite için
    );

    if (imageBytes != null) {
      final imageFile = File('$directory/$fileName');
      await imageFile.writeAsBytes(imageBytes);
      await Share.shareXFiles([XFile(imageFile.path)], text: 'Dijital Kartvizitim');
    }
  }

  // vCard paylaşımını kaldırdık veya sadece görsel paylaşımı ana odak yaptık.
  // Kullanıcı vCard metnini görmek istemiyordu.

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(
        title: const Text("Kart Detayı"),
        elevation: 0,
        backgroundColor: Colors.transparent,
        foregroundColor: Colors.black,
        actions: [
          IconButton(
            icon: const Icon(Icons.edit),
            onPressed: () async {
              final result = await Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => CardEditorView(card: _currentCard)),
              );
              if (result == true) {
                final userCards = await _dbService.getCards(_currentCard.userId);
                setState(() {
                  _currentCard = userCards.firstWhere((c) => c.id == _currentCard.id);
                });
              }
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            // NFC Durum Göstergesi
            Container(
              padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
              margin: const EdgeInsets.only(bottom: 20),
              decoration: BoxDecoration(
                color: _isNfcAvailable ? Colors.green.withOpacity(0.1) : Colors.red.withOpacity(0.1),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: _isNfcAvailable ? Colors.green : Colors.red, width: 1.5),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    _isNfcAvailable ? Icons.nfc : Icons.nfc_outlined,
                    size: 18,
                    color: _isNfcAvailable ? Colors.green : Colors.red,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    _isNfcAvailable ? "NFC Paylaşım Aktif" : "NFC Kapalı / Yok",
                    style: TextStyle(
                      color: _isNfcAvailable ? Colors.green : Colors.red,
                      fontWeight: FontWeight.bold,
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
            ),

            // Paylaşılabilir Kart Tasarımı (Görsele Uygun Düzenlendi)
            Screenshot(
              controller: _screenshotController,
              child: Container(
                width: double.infinity,
                height: 230, // Yüksekliği biraz artırdık
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: Color(int.parse(_currentCard.cardColor)),
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.08),
                      blurRadius: 15,
                      offset: const Offset(0, 5),
                    ),
                  ],
                ),
                child: Stack(
                  children: [
                    // Sağ Üst Dekoratif Daire (Görsele sadık kalmak için)
                    Positioned(
                      right: -30,
                      top: -30,
                      child: Container(
                        width: 150,
                        height: 150,
                        decoration: BoxDecoration(
                          color: Colors.black.withOpacity(0.03),
                          shape: BoxShape.circle,
                        ),
                      ),
                    ),

                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // İsim ve Soyisim (Daha büyük ve belirgin)
                        Text(
                          _currentCard.name.toUpperCase(),
                          style: const TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.w900,
                            color: Color(0xFF2C3E50),
                            letterSpacing: 0.5,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.visible, // Tam görünmesi için
                        ),
                        const SizedBox(height: 4),
                        // Ünvan
                        Text(
                          _currentCard.title,
                          style: const TextStyle(
                            fontSize: 16,
                            color: Colors.black87,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                        // Şirket
                        Text(
                          _currentCard.company,
                          style: TextStyle(
                            fontSize: 14,
                            color: Colors.black.withOpacity(0.6),
                            fontWeight: FontWeight.bold,
                          ),
                        ),

                        const Spacer(),

                        // Alt Bilgiler (Adres, Email, Telefon)
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.end,
                          children: [
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(_currentCard.address, style: const TextStyle(fontSize: 12, color: Colors.black87)),
                                  Text(_currentCard.email, style: const TextStyle(fontSize: 12, color: Colors.black87)),
                                  Text(_currentCard.phones, style: const TextStyle(fontSize: 12, color: Colors.black87, fontWeight: FontWeight.bold)),
                                ],
                              ),
                            ),

                            // QR Kod (Sağ Alt)
                            Container(
                              padding: const EdgeInsets.all(6),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(12),
                                boxShadow: [
                                  BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 5),
                                ],
                              ),
                              child: QrImageView(
                                data: 'BEGIN:VCARD\nVERSION:3.0\nFN:${_currentCard.name}\nTEL:${_currentCard.phones}\nEMAIL:${_currentCard.email}\nEND:VCARD',
                                version: QrVersions.auto,
                                size: 70.0,
                                padding: EdgeInsets.zero,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 40),

            // Sadece Görsel Paylaş Butonu (vCard metni kaldırıldı)
            SizedBox(
              width: double.infinity,
              height: 60,
              child: ElevatedButton.icon(
                onPressed: _shareCardImage,
                icon: const Icon(Icons.share_rounded, color: Colors.white, size: 28),
                label: const Text(
                  "KARTVİZİTİ PAYLAŞ",
                  style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF2C3E50),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  elevation: 4,
                ),
              ),
            ),

            const SizedBox(height: 20),
            const Text(
              "Paylaş butonuna bastığınızda kartınız yüksek kaliteli bir resim olarak gönderilir.",
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey, fontSize: 12),
            ),
          ],
        ),
      ),
    );
  }
}
