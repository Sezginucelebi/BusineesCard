import 'dart:io';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../models/business_card.dart';

class BusinessCardItem extends StatelessWidget {
  final BusinessCard card;
  final VoidCallback? onTap;
  final VoidCallback? onEdit;
  final VoidCallback? onDelete;
  final VoidCallback? onLogoTap;
  final bool showActions;

  const BusinessCardItem({
    super.key,
    required this.card,
    this.onTap,
    this.onEdit,
    this.onDelete,
    this.onLogoTap,
    this.showActions = true,
  });

  @override
  Widget build(BuildContext context) {
    // Kart rengini ayarla
    final cardColor = Color(card.cardColor);
    
    // Renk parlaklığını hesapla (Açık tonda koyu yazı, koyu tonda açık yazı)
    final bool isLightColor = cardColor.computeLuminance() > 0.5;
    final contentColor = isLightColor ? const Color(0xFF333333) : Colors.white;
    final secondaryColor = contentColor.withOpacity(0.7);
    final tertiaryColor = contentColor.withOpacity(0.5);

    // Seçilen font stilini uygula
    TextStyle getTextStyle(double size, {FontWeight weight = FontWeight.normal}) {
      TextStyle baseStyle = TextStyle(
        fontSize: size,
        color: contentColor,
        fontWeight: weight,
      );

      switch (card.fontStyle) {
        case 'Serif':
          return GoogleFonts.notoSerif(textStyle: baseStyle);
        case 'Monospace':
          return GoogleFonts.robotoMono(textStyle: baseStyle);
        case 'SansSerif':
          return GoogleFonts.openSans(textStyle: baseStyle);
        default:
          return GoogleFonts.poppins(textStyle: baseStyle);
      }
    }

    return Card(
      elevation: 6,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Container(
          height: 210, // Android'deki %10 artırılmış yükseklik
          width: double.infinity,
          color: cardColor,
          padding: const EdgeInsets.all(20),
          child: Stack(
            children: [
              // Arka plan dekoratif halka (Android'deki Canvas çizimi gibi)
              Positioned(
                right: -40,
                top: -40,
                child: Container(
                  width: 140,
                  height: 140,
                  decoration: BoxDecoration(
                    color: contentColor.withOpacity(0.05),
                    shape: BoxShape.circle,
                  ),
                ),
              ),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // SOL TARAF: İletişim ve Kişisel Bilgiler
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // İsim ve Ünvan Grubu
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                card.name,
                                style: getTextStyle(22, weight: FontWeight.bold),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                              Text(
                                card.title,
                                style: getTextStyle(15, weight: FontWeight.w500),
                              ),
                              if (card.company.isNotEmpty)
                                Padding(
                                  padding: const EdgeInsets.only(top: 4),
                                  child: Text(
                                    card.company,
                                    style: getTextStyle(13, weight: FontWeight.w600),
                                  ),
                                ),
                            ],
                          ),
                        ),
                        // Alt Bilgiler: Adres, Email, Tel
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            if (card.address.isNotEmpty)
                              Text(card.address, style: getTextStyle(11), maxLines: 1),
                            if (card.email.isNotEmpty)
                              Text(card.email, style: getTextStyle(11)),
                            if (card.phones.isNotEmpty)
                              Text(card.phones.first, style: getTextStyle(11)),
                          ],
                        ),
                      ],
                    ),
                  ),
                  
                  // SAĞ TARAF: Logo ve İşlem Butonları
                  Column(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      // Logo Alanı (Tıklanabilir)
                      GestureDetector(
                        onTap: onLogoTap,
                        child: Container(
                          width: 60,
                          height: 60,
                          decoration: BoxDecoration(
                            color: contentColor.withOpacity(0.1),
                            shape: BoxShape.circle,
                            image: card.photoUri != null
                                ? DecorationImage(
                                    image: FileImage(File(card.photoUri!)),
                                    fit: BoxFit.cover,
                                  )
                                : null,
                          ),
                          child: card.photoUri == null
                              ? Icon(Icons.add, color: contentColor.withOpacity(0.2), size: 30)
                              : null,
                        ),
                      ),
                      
                      // Düzenle/Sil Butonları (Sadece Listede Görünür)
                      if (showActions)
                        Row(
                          children: [
                            IconButton(
                              icon: Icon(Icons.edit_outlined, color: tertiaryColor, size: 22),
                              onPressed: onEdit,
                              padding: EdgeInsets.zero,
                              constraints: const BoxConstraints(),
                            ),
                            const SizedBox(width: 8),
                            IconButton(
                              icon: Icon(Icons.delete_outline, color: isLightColor ? const Color(0xFFB71C1C) : const Color(0xFFEF5350), size: 22),
                              onPressed: onDelete,
                              padding: EdgeInsets.zero,
                              constraints: const BoxConstraints(),
                            ),
                          ],
                        ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}