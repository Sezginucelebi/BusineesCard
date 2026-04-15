import 'package:flutter/material.dart';
import '../models/business_card.dart';
import '../services/database_service.dart';
import '../services/auth_service.dart';
import 'card_editor_view.dart';
import 'card_details_view.dart';
import 'login_view.dart';
import 'store_view.dart';

class CardListView extends StatefulWidget {
  const CardListView({super.key});

  @override
  State<CardListView> createState() => _CardListViewState();
}

class _CardListViewState extends State<CardListView> {
  final DatabaseService _dbService = DatabaseService();
  final AuthService _authService = AuthService();
  List<BusinessCard> _cards = [];
  bool _isLoading = true;
  SubscriptionLevel _subLevel = SubscriptionLevel.none;
  int _cardLimit = 1;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _isLoading = true);
    final user = _authService.currentUser;
    if (user != null) {
      final cards = await _dbService.getCards(user.uid);
      final level = await _authService.getSubscriptionLevel();
      final limit = await _authService.getCardLimit();
      setState(() {
        _cards = cards;
        _subLevel = level;
        _cardLimit = limit;
        _isLoading = false;
      });
    }
  }

  void _addNewCard() async {
    if (_cards.length >= _cardLimit) {
      _showPremiumDialog();
      return;
    }

    final result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const CardEditorView()),
    );
    if (result == true) _loadData();
  }

  void _showPremiumDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Limit Aşıldı"),
        content: Text(
            "Mevcut paketinizle en fazla $_cardLimit kart oluşturabilirsiniz. Daha fazlası için Mağaza'ya göz atın!"),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Vazgeç")),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(context);
              await Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const StoreView()),
              );
              _loadData();
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.amber),
            child: const Text("Mağaza'ya Git", style: TextStyle(color: Colors.black)),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Kartvizitlerim"),
        actions: [
          IconButton(
            icon: const Icon(Icons.store, color: Colors.amber),
            onPressed: () async {
              await Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const StoreView()),
              );
              _loadData();
            },
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await _authService.signOut();
              if (mounted) {
                Navigator.pushReplacement(
                  context,
                  MaterialPageRoute(builder: (context) => const LoginView()),
                );
              }
            },
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _cards.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.contact_mail_outlined,
                          size: 80, color: Colors.grey),
                      const SizedBox(height: 10),
                      const Text("Henüz bir kart eklemediniz.",
                          style: TextStyle(color: Colors.grey)),
                      const SizedBox(height: 20),
                      ElevatedButton(
                        onPressed: _addNewCard,
                        child: const Text("İlk Kartını Oluştur"),
                      )
                    ],
                  ),
                )
              : Column(
                  children: [
                    // Limit Bilgi Barı
                    Container(
                      padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
                      color: Colors.blueGrey.shade50,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text("Paket: ${_subLevel.name.toUpperCase()}", style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                          Text("Kart Sayısı: ${_cards.length} / $_cardLimit", style: const TextStyle(fontSize: 12)),
                        ],
                      ),
                    ),
                    Expanded(
                      child: ListView.builder(
                        padding: const EdgeInsets.all(10),
                        itemCount: _cards.length,
                        itemBuilder: (context, index) {
                          final card = _cards[index];
                          return Card(
                            elevation: 2,
                            margin: const EdgeInsets.only(bottom: 10),
                            shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12)),
                            child: ListTile(
                              leading: CircleAvatar(
                                backgroundColor: Color(int.parse(card.cardColor)),
                                child: Text(card.name[0].toUpperCase()),
                              ),
                              title: Text(card.name,
                                  style: const TextStyle(fontWeight: FontWeight.bold)),
                              subtitle: Text("${card.title} - ${card.company}"),
                              trailing: IconButton(
                                icon: const Icon(Icons.delete_outline, color: Colors.red),
                                onPressed: () async {
                                  bool confirm = await showDialog(
                                    context: context,
                                    builder: (context) => AlertDialog(
                                      title: const Text("Kartı Sil"),
                                      content: const Text("Bu kartı silmek istediğinize emin misiniz?"),
                                      actions: [
                                        TextButton(onPressed: () => Navigator.pop(context, false), child: const Text("İptal")),
                                        TextButton(onPressed: () => Navigator.pop(context, true), child: const Text("Sil", style: TextStyle(color: Colors.red))),
                                      ],
                                    ),
                                  );
                                  if (confirm == true) {
                                    await _dbService.deleteCard(card.id!);
                                    _loadData();
                                  }
                                },
                              ),
                              onTap: () async {
                                final result = await Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                      builder: (context) => CardDetailsView(card: card)),
                                );
                                if (result == true) _loadData();
                              },
                            ),
                          );
                        },
                      ),
                    ),
                  ],
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: _addNewCard,
        backgroundColor: const Color(0xFF2C3E50),
        child: const Icon(Icons.add, color: Colors.white),
      ),
    );
  }
}
