class BusinessCard {
  final int? id;
  final String name;
  final String title;
  final String company;
  final String address;
  final String phones;
  final String email;
  final String website;
  final String? photoPath;
  final String cardColor;
  final String fontStyle;
  final String userId;

  BusinessCard({
    this.id,
    required this.name,
    required this.title,
    required this.company,
    required this.address,
    required this.phones,
    required this.email,
    required this.website,
    this.photoPath,
    required this.cardColor,
    required this.fontStyle,
    required this.userId,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'title': title,
      'company': company,
      'address': address,
      'phones': phones,
      'email': email,
      'website': website,
      'photoPath': photoPath,
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
      company: map['company'],
      address: map['address'],
      phones: map['phones'],
      email: map['email'],
      website: map['website'],
      photoPath: map['photoPath'],
      cardColor: map['cardColor'],
      fontStyle: map['fontStyle'],
      userId: map['userId'],
    );
  }
}
