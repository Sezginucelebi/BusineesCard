import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';
import '../../models/business_card.dart';
import '../../services/database_helper.dart';

class CardEditorScreen extends StatefulWidget {
  final String userId;
  final BusinessCard? card; // Eğer düzenleme yapılıyorsa bu dolu gelir

  const CardEditorScreen({super.key, required this.userId, this.card});

  @override
  State<CardEditorScreen> createState() => _CardEditorScreenState();
}

class _CardEditorScreenState extends State<CardEditorScreen> {
  final _formKey = GlobalKey<FormState>();
  
  // Input Kontrolcüleri
  late TextEditingController _nameController;
  late TextEditingController _titleController;
  late TextEditingController _companyController;
  late TextEditingController _addressController;
  late TextEditingController _emailController;
  late TextEditingController _websiteController;
  
  // Dinamik Telefon Listesi
  List<TextEditingController> _phoneControllers = [];
  
  // Stil Ayarları
  int _selectedColor = 0xFFF5F5F5;
  String _selectedFont = 'Default';

  // Android'deki Renk Temalarımız
  final List<int> _themes = [
    0xFFE3F2FD, // Ice Blue
    0xFFE8F5E9, // Soft Green
    0xFFFFF3E0, // Peach
    0xFFF3E5F5, // Lavender
    0xFFFAFAFA, // Pearl White
  ];

  final List<String> _fonts = ['Default', 'Serif', 'SansSerif', 'Monospace'];

  @override
  void initState() {
    super.initState();
    // Eğer düzenleme modundaysak mevcut verileri doldur
    _nameController = TextEditingController(text: widget.card?.name ?? '');
    _titleController = TextEditingController(text: widget.card?.title ?? '');
    _companyController = TextEditingController(text: widget.card?.company ?? '');
    _addressController = TextEditingController(text: widget.card?.address ?? '');
    _emailController = TextEditingController(text: widget.card?.email ?? '');
    _websiteController = TextEditingController(text: widget.card?.website ?? '');
    
    _selectedColor = widget.card?.cardColor ?? 0xFFFAFAFA;
    _selectedFont = widget.card?.fontStyle ?? 'Default';

    if (widget.card != null && widget.card!.phones.isNotEmpty) {
      _phoneControllers = widget.card!.phones
          .map((p) => TextEditingController(text: p))
          .toList();
    } else {
      _phoneControllers = [TextEditingController()];
    }
  }

  void _saveCard() async {
    if (_formKey.currentState!.validate()) {
      final card = BusinessCard(
        id: widget.card?.id ?? const Uuid().v4(),
        userId: widget.userId,
        name: _nameController.text,
        title: _titleController.text,
        company: _companyController.text,
        address: _addressController.text,
        email: _emailController.text,
        website: _websiteController.text,
        phones: _phoneControllers.map((c) => c.text).where((t) => t.isNotEmpty).toList(),
        cardColor: _selectedColor,
        fontStyle: _selectedFont,
        photoUri: widget.card?.photoUri, // Logo şimdilik aynı kalsın
      );

      await DatabaseHelper().insertCard(card);
      if (mounted) Navigator.pop(context);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.card == null ? "Yeni Kartvizit" : "Kartı Düzenle"),
        actions: [
          IconButton(icon: const Icon(Icons.check), onPressed: _saveCard),
        ],
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _buildSectionTitle("Kişisel Bilgiler"),
            _buildTextField(_nameController, "Ad Soyad", Icons.person, isRequired: true),
            _buildTextField(_titleController, "Ünvan", Icons.work, isRequired: true),
            _buildTextField(_companyController, "Şirket", Icons.business),
            
            _buildSectionTitle("İletişim Bilgileri"),
            ..._phoneControllers.asMap().entries.map((entry) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: _buildTextField(entry.value, "Telefon ${entry.key + 1}", Icons.phone, 
                  isPhone: true,
                  suffix: IconButton(
                    icon: const Icon(Icons.remove_circle_outline, color: Colors.red),
                    onPressed: () => setState(() => _phoneControllers.removeAt(entry.key)),
                  )),
              );
            }),
            TextButton.icon(
              onPressed: () => setState(() => _phoneControllers.add(TextEditingController())),
              icon: const Icon(Icons.add),
              label: const Text("Telefon Ekle"),
            ),
            _buildTextField(_emailController, "E-posta", Icons.email),
            _buildTextField(_addressController, "Adres", Icons.map),
            _buildTextField(_websiteController, "Web Sitesi", Icons.language),

            _buildSectionTitle("Görünüm Ayarları"),
            const Text("Kart Rengi", style: TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            SizedBox(
              height: 50,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: _themes.length,
                itemBuilder: (context, index) {
                  final colorInt = _themes[index];
                  return GestureDetector(
                    onTap: () => setState(() => _selectedColor = colorInt),
                    child: Container(
                      width: 50,
                      margin: const EdgeInsets.only(right: 10),
                      decoration: BoxDecoration(
                        color: Color(colorInt),
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: _selectedColor == colorInt ? Colors.blue : Colors.grey.shade300,
                          width: 3,
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 16),
            const Text("Yazı Tipi", style: TextStyle(fontWeight: FontWeight.bold)),
            DropdownButton<String>(
              value: _selectedFont,
              isExpanded: true,
              items: _fonts.map((f) => DropdownMenuItem(value: f, child: Text(f))).toList(),
              onChanged: (val) => setState(() => _selectedFont = val!),
            ),
            const SizedBox(height: 100), // Alt taraf boşluk
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Text(title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.blueGrey)),
    );
  }

  Widget _buildTextField(TextEditingController controller, String label, IconData icon, {bool isRequired = false, Widget? suffix, bool isPhone = false}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: TextFormField(
        controller: controller,
        keyboardType: isPhone ? TextInputType.phone : TextInputType.text,
        inputFormatters: isPhone ? [PhoneInputFormatter()] : null,
        decoration: InputDecoration(
          labelText: label,
          hintText: isPhone ? "XXXX XXX XX XX" : null,
          contentPadding: const EdgeInsets.symmetric(vertical: 18, horizontal: 16),
          prefixIcon: Icon(icon),
          suffixIcon: suffix,
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
        ),
        validator: isRequired ? (val) => val!.isEmpty ? "Bu alan boş bırakılamaz" : null : null,
      ),
    );
  }
}

class PhoneInputFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(TextEditingValue oldValue, TextEditingValue newValue) {
    final text = newValue.text.replaceAll(' ', '');
    if (text.length > 11) return oldValue;

    String formatted = '';
    for (int i = 0; i < text.length; i++) {
      formatted += text[i];
      if (i == 3 || i == 6 || i == 8) {
        if (i != text.length - 1) formatted += ' ';
      }
    }

    return TextEditingValue(
      text: formatted,
      selection: TextSelection.collapsed(offset: formatted.length),
    );
  }
}
