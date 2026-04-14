import 'dart:ui';

class BusinessCard {
  final String id;
  final String name;
  final String title;  final String company;
  final String address;
  final List<String> phones;
  final String email;
  final String website;
  final String? photoUri;
  final int cardColor;
  final String fontStyle;
  final String userId;

  BusinessCard({
    required this.id,
    required this.name,
    required this.title,
    this.company = '',
    this.address = '',
    this.phones = const [],
    this.email = '',
    this.website = '',
    this.photoUri,
    this.cardColor = 0xFFF5F5F5,
    this.fontStyle = 'Default',
    required this.userId,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'title': title,
      'company': company,
      'address': address,
      'phones': phones.join(','), 
      'email': email,
      'website': website,
      'photoUri': photoUri,
      'cardColor': cardColor,
      'fontStyle': fontStyle,
      'userId': userId,
    };
  }

  factory BusinessCard.fromMap(Map<String, dynamic> map) {
    return BusinessCard(
      id: map['id'],
      name: map['name'],
      title: map['title'],
      company: map['company'] ?? '',
      address: map['address'] ?? '',
      phones: (map['phones'] as String).split(','),
      email: map['email'] ?? '',
      website: map['website'] ?? '',
      photoUri: map['photoUri'],
      cardColor: map['cardColor'],
      fontStyle: map['fontStyle'] ?? 'Default',
      userId: map['userId'],
    );
  }
}