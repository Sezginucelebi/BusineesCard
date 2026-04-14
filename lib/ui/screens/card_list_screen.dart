import 'package:flutter/material.dart';
import '../../models/business_card.dart';
import '../../services/database_helper.dart';
import '../widgets/business_card_item.dart';
import 'card_editor_screen.dart';
import 'preview_screen.dart';

class CardListScreen extends StatefulWidget {
  final String userId;
  const CardListScreen({super.key, required this.userId});

  @override
  State<CardListScreen> createState() => _CardListScreenState();
}

class _CardListScreenState extends State<CardListScreen> {
  late Future<List<BusinessCard>> _cardsFuture;

  @override
  void initState() {
    super.initState();
    _refreshCards();
  }

  // Listeyi veritabanından tekrar çekmek için
  void _refreshCards() {
    setState(() {
      _cardsFuture = DatabaseHelper().getCards(widget.userId);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Kartlarım"),
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.logout, color: Colors.redAccent),
            onPressed: () {
              // Buraya ileride Firebase Logout eklenebilir
              Navigator.pop(context); 
            },
          ),
        ],
      ),
      body: FutureBuilder<List<BusinessCard>>(
        future: _cardsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          
          if (snapshot.hasError) {
            return Center(child: Text("Hata: ${snapshot.error}"));
          }

          if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.credit_card_off, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text("Henüz bir kart oluşturmadınız.", 
                       style: TextStyle(color: Colors.grey)),
                ],
              ),
            );
          }

          final cards = snapshot.data!;
          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: cards.length,
            separatorBuilder: (context, index) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final card = cards[index];
              return BusinessCardItem(
                card: card,
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => PreviewScreen(card: card),
                    ),
                  );
                },
                onEdit: () async {
                  await Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => CardEditorScreen(
                        userId: widget.userId, 
                        card: card
                      ),
                    ),
                  );
                  _refreshCards(); // Dönünce listeyi güncelle
                },
                onDelete: () {
                  _showDeleteConfirmDialog(card);
                },
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          await Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => CardEditorScreen(userId: widget.userId),
            ),
          );
          _refreshCards();
        },
        child: const Icon(Icons.add),
      ),
    );
  }

  // Silme onayı penceresi
  void _showDeleteConfirmDialog(BusinessCard card) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Kartı Sil"),
        content: Text("${card.name} isimli kartı silmek istediğinizden emin misiniz?"),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("İptal"),
          ),
          TextButton(
            onPressed: () async {
              await DatabaseHelper().deleteCard(card.id);
              Navigator.pop(context);
              _refreshCards();
            },
            child: const Text("Sil", style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }
}