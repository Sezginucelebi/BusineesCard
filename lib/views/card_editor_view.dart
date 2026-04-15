import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../models/business_card.dart';
import '../services/database_service.dart';
import '../services/auth_service.dart';

class CardEditorView extends StatefulWidget {
  final BusinessCard? card;
  const CardEditorView({super.key, this.card});

  @override
  State<CardEditorView> createState() => _CardEditorViewState();
}

class _CardEditorViewState extends State<CardEditorView> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _titleController;
  late TextEditingController _companyController;
  late TextEditingController _phoneController;
  late TextEditingController _emailController;
  late TextEditingController _websiteController;
  late TextEditingController _addressController;

  String _selectedColor = "0xFFE3F2FD";
  final DatabaseService _dbService = DatabaseService();
  final AuthService _authService = AuthService();

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.card?.name ?? "");
    _titleController = TextEditingController(text: widget.card?.title ?? "");
    _companyController = TextEditingController(text: widget.card?.company ?? "");
    _phoneController = TextEditingController(text: widget.card?.phones ?? "");
    _emailController = TextEditingController(text: widget.card?.email ?? "");
    _websiteController = TextEditingController(text: widget.card?.website ?? "");
    _addressController = TextEditingController(text: widget.card?.address ?? "");
    _selectedColor = widget.card?.cardColor ?? "0xFFE3F2FD";
  }

  @override
  void dispose() {
    _nameController.dispose();
    _titleController.dispose();
    _companyController.dispose();
    _phoneController.dispose();
    _emailController.dispose();
    _websiteController.dispose();
    _addressController.dispose();
    super.dispose();
  }

  void _onSave() async {
    if (_formKey.currentState!.validate()) {
      final user = _authService.currentUser;
      if (user == null) return;

      final newCard = BusinessCard(
        id: widget.card?.id,
        name: _nameController.text,
        title: _titleController.text,
        company: _companyController.text,
        address: _addressController.text,
        phones: _phoneController.text,
        email: _emailController.text,
        website: _websiteController.text,
        cardColor: _selectedColor,
        fontStyle: "Default",
        userId: user.uid,
      );

      if (widget.card == null) {
        await _dbService.insertCard(newCard);
      } else {
        await _dbService.updateCard(newCard);
      }

      if (mounted) {
        Navigator.pop(context, true);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.card == null ? "Yeni Kart" : "Kartı Düzenle"),
        actions: [
          IconButton(onPressed: _onSave, icon: const Icon(Icons.check)),
        ],
      ),
      body: Form(
        key: _formKey,
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text("Kişisel Bilgiler", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
              const SizedBox(height: 10),
              _buildTextField(_nameController, "Ad Soyad", Icons.person),
              _buildTextField(_titleController, "Ünvan", Icons.work),
              _buildTextField(_companyController, "Şirket", Icons.business),

              const SizedBox(height: 20),
              const Text("İletişim Bilgileri", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
              const SizedBox(height: 10),
              _buildPhoneField(),
              _buildTextField(_emailController, "E-posta", Icons.email, keyboardType: TextInputType.emailAddress),
              _buildTextField(_websiteController, "Web Sitesi", Icons.language, keyboardType: TextInputType.url),
              _buildTextField(_addressController, "Adres", Icons.location_on, maxLines: 3),

              const SizedBox(height: 20),
              const Text("Kart Rengi", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
              const SizedBox(height: 10),
              _buildColorPicker(),

              const SizedBox(height: 30),
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton(
                  onPressed: _onSave,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF2C3E50),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                  child: const Text("KAYDET", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTextField(TextEditingController controller, String label, IconData icon, {TextInputType? keyboardType, int maxLines = 1}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 15),
      child: TextFormField(
        controller: controller,
        decoration: InputDecoration(
          labelText: label,
          prefixIcon: Icon(icon),
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
        ),
        keyboardType: keyboardType,
        maxLines: maxLines,
        validator: (value) => value == null || value.isEmpty ? "$label boş bırakılamaz" : null,
      ),
    );
  }

  Widget _buildPhoneField() {
    return Padding(
      padding: const EdgeInsets.only(bottom: 15),
      child: TextFormField(
        controller: _phoneController,
        decoration: InputDecoration(
          labelText: "Telefon (XXXX XXX XX XX)",
          prefixIcon: const Icon(Icons.phone),
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
        ),
        keyboardType: TextInputType.phone,
        inputFormatters: [
          FilteringTextInputFormatter.digitsOnly,
          _PhoneNumberFormatter(),
        ],
        validator: (value) => value == null || value.isEmpty ? "Telefon boş bırakılamaz" : null,
      ),
    );
  }

  Widget _buildColorPicker() {
    final colors = [
      "0xFFE3F2FD", // Ice Blue
      "0xFFE8F5E9", // Soft Green
      "0xFFFFF3E0", // Peach
      "0xFFF3E5F5", // Lavender
      "0xFFFAFAFA", // White
      "0xFF2C2C2C", // Dark
    ];

    return SizedBox(
      height: 60,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        itemCount: colors.length,
        itemBuilder: (context, index) {
          final colorCode = colors[index];
          final isSelected = _selectedColor == colorCode;
          return GestureDetector(
            onTap: () => setState(() => _selectedColor = colorCode),
            child: Container(
              width: 50,
              margin: const EdgeInsets.only(right: 10),
              decoration: BoxDecoration(
                color: Color(int.parse(colorCode)),
                shape: BoxShape.circle,
                border: Border.all(
                  color: isSelected ? Colors.blue : Colors.grey.shade300,
                  width: isSelected ? 3 : 1,
                ),
              ),
              child: isSelected ? const Icon(Icons.check, color: Colors.blue) : null,
            ),
          );
        },
      ),
    );
  }
}

class _PhoneNumberFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(TextEditingValue oldValue, TextEditingValue newValue) {
    final text = newValue.text;
    if (text.length > 11) return oldValue;

    final buffer = StringBuffer();
    for (int i = 0; i < text.length; i++) {
      buffer.write(text[i]);
      if ((i == 3 || i == 6 || i == 8) && i != text.length - 1) {
        buffer.write(" ");
      }
    }

    final String string = buffer.toString();
    return newValue.copyWith(
      text: string,
      selection: TextSelection.collapsed(offset: string.length),
    );
  }
}
